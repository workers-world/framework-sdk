package com.books.framework.exception;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.message.BaseMessage;
import com.books.framework.trace.TraceIdUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerMockMvcTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void clearTrace() {
        TraceIdUtils.clear();
    }

    @Test
    void appExceptionReturnsBaseMessageJson() throws Exception {
        TraceIdUtils.set("mvc-trace-1");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestApiController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/test/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCodeEnum.DATA_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("not found"))
                .andExpect(jsonPath("$.traceId").value("mvc-trace-1"));
    }

    @Test
    void requestBodyBaseMessagePropagatesMessageIdOnError() throws Exception {
        TraceIdUtils.set("mvc-trace-2");
        BaseMessage request = BaseMessage.newRequest("req-77", "app", "2.0");
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new TestApiController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(post("/test/echo-fail")
                        .requestAttr(BaseMessage.class.getName(), request)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("req-77"))
                .andExpect(jsonPath("$.code").value(ErrorCodeEnum.PARAM_ERROR.getCode()))
                .andExpect(jsonPath("$.traceId").value("mvc-trace-2"));
    }

    @RestController
    static class TestApiController {

        @PostMapping("/test/fail")
        public BaseMessage fail() {
            throw new AppException(ErrorCodeEnum.DATA_NOT_FOUND, "not found");
        }

        @PostMapping("/test/echo-fail")
        public BaseMessage echoFail(@RequestBody BaseMessage request) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "invalid");
        }
    }
}
