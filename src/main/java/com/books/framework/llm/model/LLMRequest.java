package com.books.framework.llm.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class LLMRequest {
    private String systemPrompt;
    private String userMessage;
    private String responseFormat;
    private String jsonSchema;
    private String provider;
    private boolean skipSanitize;
    private List<String> enabledSanitizers;
    private String idempotencyKey;
    private int maxTokens;
    private double temperature;
    private Map<String, Object> metadata;

    public static class LLMRequestBuilder {
        private int maxTokens = 256;
        private double temperature = 0.1;
        private boolean skipSanitize = false;
    }
}
