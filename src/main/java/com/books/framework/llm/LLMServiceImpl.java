package com.books.framework.llm;

import com.books.framework.llm.config.LLMProperties;
import com.books.framework.llm.log.LLMCallLogEntry;
import com.books.framework.llm.log.LLMCallLogger;
import com.books.framework.llm.model.LLMBackendConfig;
import com.books.framework.llm.model.LLMRequest;
import com.books.framework.llm.model.LLMResponse;
import com.books.framework.llm.rate.CircuitBreaker;
import com.books.framework.llm.rate.RateLimiter;
import com.books.framework.llm.sanitizer.Sanitizer;
import com.books.framework.llm.sanitizer.SanitizerChain;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.ResponseFormat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
public class LLMServiceImpl implements LLMService {

    private final Map<String, ChatClient> chatClientMap;
    private final Map<String, LLMBackendConfig> configMap;
    private final SanitizerChain sanitizerChain;
    private final RateLimiter rateLimiter;
    private final CircuitBreaker circuitBreaker;
    private final LLMCallLogger callLogger;
    private final LLMProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(4);
    private final Map<String, Future<LLMResponse>> asyncTasks = new ConcurrentHashMap<>();

    public LLMServiceImpl(Map<String, ChatClient> chatClientMap,
                          List<LLMBackendConfig> configs,
                          List<Sanitizer> sanitizers,
                          LLMCallLogger callLogger,
                          LLMProperties properties) {
        this.chatClientMap = chatClientMap;
        this.configMap = new HashMap<>();
        for (LLMBackendConfig c : configs) {
            this.configMap.put(c.getProvider(), c);
        }
        this.sanitizerChain = new SanitizerChain(sanitizers);
        this.callLogger = callLogger;
        this.properties = properties;
        this.rateLimiter = new RateLimiter(properties.getDailyLimit(), properties.getPerMinuteLimit());
        this.circuitBreaker = new CircuitBreaker(5, 60);
    }

    @Override
    public LLMResponse execute(LLMRequest request) {
        // 幂等检查
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isEmpty()) {
            LLMCallLogEntry cached = callLogger.findByIdempotencyKey(request.getIdempotencyKey());
            if (cached != null) {
                return LLMResponse.builder()
                        .success(true).content(cached.getResponseText())
                        .parsedContent(cached.getParsedResult())
                        .tokensUsed(cached.getTokensUsed()).cost(cached.getCost())
                        .responseTimeMs(cached.getResponseTimeMs())
                        .modelName(cached.getModelName()).provider(cached.getProvider())
                        .fromCache(true).build();
            }
        }

        String provider = resolveProvider(request);
        ChatClient chatClient = chatClientMap.get(provider);
        if (chatClient == null) {
            return fail("未找到后端: " + provider, provider, null);
        }
        LLMBackendConfig config = configMap.get(provider);

        // 脱敏（业务特有逻辑，Spring AI 无此能力）
        String sanitizedUserMessage = request.getUserMessage();
        if (!request.isSkipSanitize()) {
            List<String> enabled = request.getEnabledSanitizers();
            if (enabled == null || enabled.isEmpty()) {
                enabled = Arrays.asList("PersonNameSanitizer", "PhoneNumberSanitizer", "BankCardSanitizer");
            }
            sanitizedUserMessage = sanitizerChain.sanitize(request.getUserMessage(), enabled);
        }
        String sanitizedSystemPrompt = request.getSystemPrompt();
        if (!request.isSkipSanitize() && sanitizedSystemPrompt != null) {
            sanitizedSystemPrompt = sanitizerChain.sanitize(sanitizedSystemPrompt, request.getEnabledSanitizers());
        }

        // 限流 + 熔断（Spring AI 无此能力）
        int maxConcurrency = config != null ? config.getMaxConcurrency() : 5;
        if (!rateLimiter.tryAcquire(provider, maxConcurrency)) {
            return fail("调用频率限制", provider, config != null ? config.getDefaultModel() : null);
        }
        if (!circuitBreaker.allowsRequest(provider)) {
            rateLimiter.release(provider);
            return fail("后端已熔断: " + provider, provider, config != null ? config.getDefaultModel() : null);
        }

        long start = System.currentTimeMillis();
        String modelName = config != null ? config.getDefaultModel() : null;
        LLMResponse response;
        try {
            OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                    .maxTokens(request.getMaxTokens() > 0 ? request.getMaxTokens() : 256)
                    .temperature(request.getTemperature());

            boolean isJsonMode = "JSON_SCHEMA".equals(request.getResponseFormat())
                    || "JSON".equals(request.getResponseFormat());

            if ("JSON_SCHEMA".equals(request.getResponseFormat()) && request.getJsonSchema() != null) {
                optionsBuilder.responseFormat(ResponseFormat.builder()
                        .type(ResponseFormat.Type.JSON_SCHEMA)
                        .jsonSchema(request.getJsonSchema())
                        .build());
            } else if ("JSON".equals(request.getResponseFormat())) {
                optionsBuilder.responseFormat(ResponseFormat.builder()
                        .type(ResponseFormat.Type.JSON_OBJECT)
                        .build());
            }

            // 使用 Spring AI ChatClient 流式 API，内置 MarkdownCodeBlockCleaner + BeanOutputConverter
            ChatClient.ChatClientRequestSpec promptSpec = chatClient.prompt()
                    .options(optionsBuilder.build());

            if (sanitizedSystemPrompt != null && !sanitizedSystemPrompt.isEmpty()) {
                promptSpec = promptSpec.system(sanitizedSystemPrompt);
            }
            promptSpec = promptSpec.user(sanitizedUserMessage);

            ChatClient.CallResponseSpec callSpec = promptSpec.call();

            String content;
            String parsedContent = null;
            if (isJsonMode) {
                // Spring AI 的 entity(Map.class) 内置 MarkdownCodeBlockCleaner + BeanOutputConverter
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> entity = callSpec.entity(Map.class);
                    content = objectMapper.writeValueAsString(entity);
                    parsedContent = content;
                } catch (Exception e) {
                    // entity() 解析失败时回退到 content()
                    content = callSpec.content();
                    parsedContent = tryParseJson(content);
                    if (content == null) content = "";
                }
            } else {
                content = callSpec.content();
                if (content == null) content = "";
            }

            // 响应脱敏
            if (!request.isSkipSanitize() && !content.isEmpty()) {
                content = sanitizerChain.sanitize(content, request.getEnabledSanitizers());
                if (parsedContent != null) {
                    parsedContent = sanitizerChain.sanitize(parsedContent, request.getEnabledSanitizers());
                }
            }

            long elapsed = System.currentTimeMillis() - start;

            // 从 chatResponse() 获取 metadata（token 用量等）
            int tokensUsed = 0;
            try {
                ChatResponse chatResp = callSpec.chatResponse();
                if (chatResp != null && chatResp.getMetadata() != null && chatResp.getMetadata().getUsage() != null) {
                    tokensUsed = (int) chatResp.getMetadata().getUsage().getTotalTokens();
                }
                if (chatResp != null && chatResp.getMetadata() != null && chatResp.getMetadata().getModel() != null) {
                    modelName = chatResp.getMetadata().getModel();
                }
            } catch (Exception ignored) {
            }

            BigDecimal cost = calcCost(tokensUsed, config);
            response = LLMResponse.builder()
                    .success(true).content(content).parsedContent(parsedContent)
                    .tokensUsed(tokensUsed).responseTimeMs(elapsed)
                    .modelName(modelName).provider(provider).cost(cost)
                    .build();
            circuitBreaker.recordSuccess(provider);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            circuitBreaker.recordFailure(provider);
            response = LLMResponse.builder()
                    .success(false).errorMessage(e.getMessage())
                    .provider(provider).modelName(modelName)
                    .responseTimeMs(elapsed).build();
        } finally {
            rateLimiter.release(provider);
        }

        // DB 审计日志（Spring AI 的 SimpleLoggerAdvisor 只打 log，不入库）
        try {
            callLogger.log(LLMCallLogEntry.builder()
                    .provider(provider).modelName(response.getModelName())
                    .requestPrompt(buildPromptLog(sanitizedSystemPrompt, sanitizedUserMessage))
                    .responseText(response.getContent()).parsedResult(response.getParsedContent())
                    .tokensUsed(response.getTokensUsed()).cost(response.getCost())
                    .responseTimeMs((int) response.getResponseTimeMs())
                    .success(response.isSuccess()).errorMessage(response.getErrorMessage())
                    .idempotencyKey(request.getIdempotencyKey())
                    .metadataJson(toJson(request.getMetadata()))
                    .build());
        } catch (Exception e) {
            log.error("LLM 日志写入失败", e);
        }

        return response;
    }

    @Override
    public Future<LLMResponse> executeAsync(LLMRequest request) {
        String taskId = UUID.randomUUID().toString();
        Future<LLMResponse> future = asyncExecutor.submit(() -> execute(request));
        asyncTasks.put(taskId, future);
        return future;
    }

    @Override
    public List<LLMResponse> executeBatch(List<LLMRequest> requests) {
        List<LLMResponse> responses = new ArrayList<>(requests.size());
        for (LLMRequest req : requests) {
            responses.add(execute(req));
        }
        return responses;
    }

    @Override
    public LLMResponse getAsyncResult(String taskId) {
        Future<LLMResponse> future = asyncTasks.get(taskId);
        if (future == null) return null;
        if (future.isDone()) {
            try { return future.get(); } catch (Exception e) {
                return LLMResponse.builder().success(false).errorMessage(e.getMessage()).build();
            }
        }
        return null;
    }

    private String resolveProvider(LLMRequest request) {
        if (request.getProvider() != null && !request.getProvider().isEmpty()) return request.getProvider();
        if (properties.getActiveProvider() != null && !properties.getActiveProvider().isEmpty())
            return properties.getActiveProvider();
        for (Map.Entry<String, LLMBackendConfig> e : configMap.entrySet()) {
            if (e.getValue().isEnabled()) return e.getKey();
        }
        return chatClientMap.isEmpty() ? "openai" : chatClientMap.keySet().iterator().next();
    }

    private LLMResponse fail(String msg, String provider, String model) {
        return LLMResponse.builder().success(false).errorMessage(msg).provider(provider).modelName(model).build();
    }

    private String buildPromptLog(String systemPrompt, String userMessage) {
        StringBuilder sb = new StringBuilder();
        if (systemPrompt != null) sb.append("[System] ").append(systemPrompt).append("\n");
        sb.append("[User] ").append(userMessage);
        return sb.toString();
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try { return objectMapper.writeValueAsString(obj); } catch (JsonProcessingException e) { return null; }
    }

    /** 仅作为 entity() 兜底使用——Spring AI 已内置 MarkdownCodeBlockCleaner + BeanOutputConverter */
    private String tryParseJson(String content) {
        if (content == null || content.isEmpty()) return null;
        try {
            Object parsed = objectMapper.readValue(content, Object.class);
            return objectMapper.writeValueAsString(parsed);
        } catch (Exception e) { return null; }
    }

    private BigDecimal calcCost(int tokensUsed, LLMBackendConfig config) {
        if (config == null || config.getCostPer1kOutput() == null) return BigDecimal.ZERO;
        return config.getCostPer1kOutput()
                .multiply(BigDecimal.valueOf(tokensUsed))
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP);
    }
}
