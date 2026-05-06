package com.books.framework.llm.controller;

import com.books.framework.annotation.ApiName;
import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.llm.LLMService;
import com.books.framework.llm.dto.*;
import com.books.framework.llm.model.LLMRequest;
import com.books.framework.llm.model.LLMResponse;
import com.books.framework.message.BaseMessage;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/llm")
public class LLMExecuteController {

    private final LLMService llmService;

    public LLMExecuteController(LLMService llmService) {
        this.llmService = llmService;
    }

    @ApiName(name = "llmExecute", input = {LLMExecuteX1.class}, output = {LLMResponseZ1.class}, description = "执行大模型调用")
    @PostMapping("/execute")
    public BaseMessage execute(@RequestBody(required = false) BaseMessage request) {
        if (request == null) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "请求报文不能为空");
        }
        LLMExecuteX1 x1 = request.getDto(LLMExecuteX1.class);
        if (x1 == null) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "请求体缺少 LLMExecuteX1");
        }
        if (x1.getUserMessage() == null || x1.getUserMessage().trim().isEmpty()) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "userMessage 不能为空");
        }

        LLMRequest llmReq = LLMRequest.builder()
                .systemPrompt(x1.getSystemPrompt())
                .userMessage(x1.getUserMessage())
                .responseFormat(x1.getResponseFormat())
                .jsonSchema(x1.getJsonSchema())
                .provider(x1.getProvider())
                .skipSanitize(x1.isSkipSanitize())
                .enabledSanitizers(x1.getEnabledSanitizers())
                .idempotencyKey(x1.getIdempotencyKey())
                .maxTokens(x1.getMaxTokens())
                .temperature(x1.getTemperature())
                .metadata(x1.getMetadata())
                .build();

        LLMResponse llmResp = llmService.execute(llmReq);

        BaseMessage response = request.newResponse();
        response.setMessage(llmResp.isSuccess() ? "调用成功" : "调用失败");
        response.addDto(LLMResponseZ1.builder()
                .content(llmResp.getContent())
                .parsedContent(llmResp.getParsedContent())
                .success(llmResp.isSuccess())
                .errorMessage(llmResp.getErrorMessage())
                .tokensUsed(llmResp.getTokensUsed())
                .responseTimeMs(llmResp.getResponseTimeMs())
                .modelName(llmResp.getModelName())
                .provider(llmResp.getProvider())
                .cost(llmResp.getCost())
                .fromCache(llmResp.isFromCache())
                .build());
        return response;
    }

    @ApiName(name = "llmExecuteBatch", input = {LLMBatchExecuteX1.class}, output = {LLMResponseZ1.class}, description = "批量大模型调用")
    @PostMapping("/execute-batch")
    public BaseMessage executeBatch(@RequestBody(required = false) BaseMessage request) {
        if (request == null) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "请求报文不能为空");
        }
        LLMBatchExecuteX1 x1 = request.getDto(LLMBatchExecuteX1.class);
        if (x1 == null || x1.getRequests() == null || x1.getRequests().isEmpty()) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "requests 不能为空");
        }

        List<LLMRequest> reqs = new ArrayList<>();
        for (LLMExecuteX1 ex : x1.getRequests()) {
            reqs.add(LLMRequest.builder()
                    .systemPrompt(ex.getSystemPrompt()).userMessage(ex.getUserMessage())
                    .responseFormat(ex.getResponseFormat()).jsonSchema(ex.getJsonSchema())
                    .provider(ex.getProvider()).skipSanitize(ex.isSkipSanitize())
                    .enabledSanitizers(ex.getEnabledSanitizers()).idempotencyKey(ex.getIdempotencyKey())
                    .maxTokens(ex.getMaxTokens()).temperature(ex.getTemperature())
                    .metadata(ex.getMetadata()).build());
        }

        List<LLMResponse> results = llmService.executeBatch(reqs);
        List<LLMResponseZ1> z1List = new ArrayList<>();
        for (LLMResponse r : results) {
            z1List.add(LLMResponseZ1.builder()
                    .content(r.getContent()).parsedContent(r.getParsedContent())
                    .success(r.isSuccess()).errorMessage(r.getErrorMessage())
                    .tokensUsed(r.getTokensUsed()).responseTimeMs(r.getResponseTimeMs())
                    .modelName(r.getModelName()).provider(r.getProvider()).cost(r.getCost())
                    .fromCache(r.isFromCache()).build());
        }

        BaseMessage response = request.newResponse();
        response.setMessage("批量调用完成");
        response.addDtoList(z1List);
        return response;
    }
}
