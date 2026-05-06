package com.books.framework.llm.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LLMResponse {
    private String content;
    private String parsedContent;
    private boolean success;
    private String errorMessage;
    private int tokensUsed;
    private long responseTimeMs;
    private String modelName;
    private String provider;
    private BigDecimal cost;
    private boolean fromCache;
}
