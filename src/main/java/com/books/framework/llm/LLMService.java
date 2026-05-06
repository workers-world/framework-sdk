package com.books.framework.llm;

import com.books.framework.llm.model.LLMRequest;
import com.books.framework.llm.model.LLMResponse;

import java.util.List;
import java.util.concurrent.Future;

public interface LLMService {
    LLMResponse execute(LLMRequest request);
    Future<LLMResponse> executeAsync(LLMRequest request);
    List<LLMResponse> executeBatch(List<LLMRequest> requests);
    LLMResponse getAsyncResult(String taskId);
}
