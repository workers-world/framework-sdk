package com.books.framework.http;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * 统一 HTTP 客户端 —— 预配置 RestClient 封装，默认 JSON + 合理超时。
 *
 * <pre>{@code
 * var client = FrameworkHttpClient.builder()
 *     .baseUrl("http://localhost:8000")
 *     .build();
 * Map<String, Object> resp = client.get("/api/health", Map.class);
 * }</pre>
 */
public class FrameworkHttpClient {

    private static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration DEFAULT_READ_TIMEOUT = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final String baseUrl;

    private FrameworkHttpClient(Builder builder) {
        this.baseUrl = builder.baseUrl != null ? stripTrailingSlash(builder.baseUrl) : "";

        Duration connect = builder.connectTimeout != null ? builder.connectTimeout : DEFAULT_CONNECT_TIMEOUT;
        Duration read = builder.readTimeout != null ? builder.readTimeout : DEFAULT_READ_TIMEOUT;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connect);
        factory.setReadTimeout(read);

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .build();
    }

    // ── GET ──────────────────────────────────────────────

    public <T> T get(String path, Class<T> responseType) {
        return get(path, responseType, Map.of());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getForMap(String path) {
        return get(path, Map.class);
    }

    public <T> T get(String path, Class<T> responseType, Map<String, String> headers) {
        return execute(() -> {
            var req = restClient.get()
                    .uri(resolveUri(path))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.forEach(req::header);
            return req.retrieve().body(responseType);
        });
    }

    // ── POST JSON ────────────────────────────────────────

    public <T> T post(String path, Object body, Class<T> responseType) {
        return execute(() -> restClient.post()
                .uri(resolveUri(path))
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(responseType));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> postForMap(String path, Object body) {
        return post(path, body, Map.class);
    }

    public <T> T post(String path, Object body, Class<T> responseType, Map<String, String> headers) {
        return execute(() -> {
            var req = restClient.post()
                    .uri(resolveUri(path))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
            headers.forEach(req::header);
            return req.retrieve().body(responseType);
        });
    }

    // ── internals ────────────────────────────────────────

    private URI resolveUri(String pathOrUrl) {
        if (pathOrUrl.startsWith("http://") || pathOrUrl.startsWith("https://")) {
            return URI.create(pathOrUrl);
        }
        return URI.create(baseUrl + pathOrUrl);
    }

    private <T> T execute(java.util.function.Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new AppException(ErrorCodeEnum.NETWORK_ERROR, e);
        }
    }

    private static String stripTrailingSlash(String url) {
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    // ── Builder ──────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String baseUrl;
        private Duration connectTimeout;
        private Duration readTimeout;
        private ObjectMapper objectMapper;

        private Builder() {
        }

        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        public Builder connectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }

        public FrameworkHttpClient build() {
            return new FrameworkHttpClient(this);
        }
    }
}
