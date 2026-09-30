package com.books.framework.idempotent;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotentInterceptorTest {

    @Mock
    private IdempotentStorage storage;

    @Test
    void skipsNonHandlerMethod() {
        IdempotentInterceptor interceptor = new IdempotentInterceptor(storage);
        assertTrue(interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object()));
    }

    @Test
    void acquiresKeyFromHeader() throws Exception {
        when(storage.tryAcquire(eq("key-abc"), eq(3600L))).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Idempotent-Key", "key-abc");

        IdempotentInterceptor interceptor = new IdempotentInterceptor(storage);
        boolean ok = interceptor.preHandle(request, new MockHttpServletResponse(), handlerMethod("idempotentMethod"));

        assertTrue(ok);
        verify(storage).tryAcquire("key-abc", 3600);
    }

    @Test
    void throwsWhenKeyMissing() throws Exception {
        IdempotentInterceptor interceptor = new IdempotentInterceptor(storage);
        AppException ex = assertThrows(AppException.class, () ->
                interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(),
                        handlerMethod("idempotentMethod")));
        assertEquals(ErrorCodeEnum.PARAM_ERROR.getCode(), ex.getErrorCode());
    }

    @Test
    void throwsWhenDuplicateRequest() throws Exception {
        when(storage.tryAcquire(eq("dup"), eq(60L))).thenReturn(false);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Idempotent-Key", "dup");

        IdempotentInterceptor interceptor = new IdempotentInterceptor(storage);
        AppException ex = assertThrows(AppException.class, () ->
                interceptor.preHandle(request, new MockHttpServletResponse(), handlerMethod("shortTtlMethod")));
        assertEquals(ErrorCodeEnum.OPERATION_IN_PROGRESS.getCode(), ex.getErrorCode());
    }

    private static HandlerMethod handlerMethod(String methodName) throws NoSuchMethodException {
        Method method = IdempotentTestController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new IdempotentTestController(), method);
    }

    static class IdempotentTestController {

        @Idempotent
        public void idempotentMethod() {
        }

        @Idempotent(ttlSeconds = 60, message = "重复")
        public void shortTtlMethod() {
        }

        public void plainMethod() {
        }
    }
}
