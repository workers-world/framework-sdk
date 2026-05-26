package com.books.framework.idempotent;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 幂等拦截器——在 Controller 方法执行前检查 {@link Idempotent} 注解。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class IdempotentInterceptor implements HandlerInterceptor {

    private static final String HEADER_IDEMPOTENT_KEY = "X-Idempotent-Key";

    private final IdempotentStorage storage;

    public IdempotentInterceptor(IdempotentStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Idempotent annotation = handlerMethod.getMethodAnnotation(Idempotent.class);
        if (annotation == null) {
            return true;
        }
        String key = resolveKey(request, annotation);
        if (key == null || key.trim().isEmpty()) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "幂等 key 不能为空（请在请求头中携带 " + HEADER_IDEMPOTENT_KEY + "）");
        }
        if (!storage.tryAcquire(key, annotation.ttlSeconds())) {
            throw new AppException(ErrorCodeEnum.OPERATION_IN_PROGRESS, annotation.message());
        }
        return true;
    }

    private String resolveKey(HttpServletRequest request, Idempotent annotation) {
        String expr = annotation.key();
        if (expr != null && !expr.trim().isEmpty()) {
            // 简单支持：格式为 "#header.name" 则从 header 取，否则直接作为 key 前缀
            if (expr.startsWith("#header.")) {
                String headerName = expr.substring("#header.".length());
                return request.getHeader(headerName);
            }
            return expr;
        }
        return request.getHeader(HEADER_IDEMPOTENT_KEY);
    }
}
