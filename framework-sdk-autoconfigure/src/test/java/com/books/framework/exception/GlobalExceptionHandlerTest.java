package com.books.framework.exception;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.message.BaseMessage;
import com.books.framework.trace.TraceIdUtils;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @AfterEach
    void clearTrace() {
        TraceIdUtils.clear();
    }

    @Test
    void handleAppExceptionReturnsApiErrorWithTraceId() {
        TraceIdUtils.set("trace-app-1");
        MockHttpServletRequest request = new MockHttpServletRequest();

        BaseMessage body = handler.handleAppException(
                new AppException(ErrorCodeEnum.DATA_NOT_FOUND, "记录不存在"), request);

        assertEquals(ErrorCodeEnum.DATA_NOT_FOUND.getCode(), body.getCode());
        assertEquals("记录不存在", body.getMessage());
        assertEquals("trace-app-1", body.getTraceId());
        assertNotNull(body.getResponseTime());
    }

    @Test
    void handleAppExceptionPreservesRequestMessageMeta() {
        BaseMessage requestMessage = BaseMessage.newRequest("msg-100", "portal", "1.0");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(BaseMessage.class.getName(), requestMessage);
        TraceIdUtils.set("trace-req-2");

        BaseMessage body = handler.handleAppException(
                new AppException(ErrorCodeEnum.PARAM_ERROR, "参数错误"), request);

        assertEquals("msg-100", body.getMessageId());
        assertEquals("portal", body.getSource());
        assertEquals(ErrorCodeEnum.PARAM_ERROR.getCode(), body.getCode());
        assertEquals("参数错误", body.getMessage());
        assertEquals("trace-req-2", body.getTraceId());
    }

    @Test
    void handleIllegalArgumentExceptionReturnsBadRequestCode() {
        TraceIdUtils.set("trace-illegal");
        BaseMessage body = handler.handleIllegalArgumentException(
                new IllegalArgumentException("bad arg"), new MockHttpServletRequest());

        assertEquals(ErrorCodeEnum.PARAM_ERROR.getCode(), body.getCode());
        assertEquals("bad arg", body.getMessage());
        assertEquals("trace-illegal", body.getTraceId());
    }

    @Test
    void handleExceptionReturnsSystemError() {
        BaseMessage body = (BaseMessage) handler.handleException(
                new RuntimeException("boom"), new MockHttpServletRequest());

        assertEquals(ErrorCodeEnum.SYSTEM_ERROR.getCode(), body.getCode());
        assertEquals("系统异常，请联系管理员", body.getMessage());
    }

    @Test
    void handleExceptionReturnsNullForEventStreamRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept", MediaType.TEXT_EVENT_STREAM_VALUE);

        assertNull(handler.handleException(new RuntimeException("boom"), request));
    }

    @Test
    void handleExceptionReturnsNullForBrokenPipe() {
        assertNull(handler.handleException(
                new IOException("Broken pipe"), new MockHttpServletRequest()));
    }

    @Test
    void handleAsyncRequestNotUsableDoesNotThrow() {
        handler.handleAsyncRequestNotUsable(
                new AsyncRequestNotUsableException("Response not usable after response errors."));
    }
}
