package com.books.framework.logging;

import com.books.framework.annotation.ApiName;
import com.books.framework.config.FrameworkProperties;
import com.books.framework.exception.AppException;
import com.books.framework.message.BaseMessage;
import com.books.framework.trace.TraceIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.List;

@Aspect
@Order(5)
public class ApiAccessLogAspect {

    private static final Logger log = LoggerFactory.getLogger("com.books.framework.logging.ApiAccessLog");

    private final FrameworkProperties.AccessLog config;
    private final AccessLogSanitizer sanitizer;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public ApiAccessLogAspect(FrameworkProperties.AccessLog config, AccessLogSanitizer sanitizer) {
        this.config = config;
        this.sanitizer = sanitizer;
    }

    @Pointcut("(@within(org.springframework.web.bind.annotation.RestController) "
            + "|| @within(org.springframework.stereotype.Controller)) "
            + "&& execution(public * *(..)) "
            + "&& (@annotation(org.springframework.web.bind.annotation.RequestMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.GetMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PostMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping) "
            + "|| @annotation(org.springframework.web.bind.annotation.PatchMapping))")
    public void controllerMappingMethods() {
    }

    @Around("controllerMappingMethods()")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!config.isEnabled()) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = currentRequest();
        if (request != null && isExcluded(request.getRequestURI())) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        ApiName apiName = method.getAnnotation(ApiName.class);
        if (config.isRequireApiName() && apiName == null) {
            return joinPoint.proceed();
        }

        String controllerName = joinPoint.getTarget().getClass().getSimpleName();
        String apiNameValue = resolveApiName(apiName, controllerName, method.getName());
        String inputType = resolveInputType(apiName);
        BaseMessage requestMessage = findFirstBaseMessage(joinPoint.getArgs());
        String messageId = requestMessage != null ? requestMessage.getMessageId() : "-";
        String httpMethod = request != null ? request.getMethod() : "-";
        String uri = request != null ? request.getRequestURI() : "-";
        String clientIp = request != null ? resolveClientIp(request) : "-";

        long startNanos = System.nanoTime();
        Object result = null;
        Throwable error = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
            String traceId = TraceIdUtils.get() != null ? TraceIdUtils.get() : "-";
            String code = resolveCode(result, error);
            String errorMessage = error != null ? error.getClass().getSimpleName() + ": " + error.getMessage() : null;

            if (error != null) {
                log.warn(
                        "api_access traceId={} apiName={} controller={} httpMethod={} uri={} messageId={} inputType={} code={} durationMs={} clientIp={} error={}",
                        traceId,
                        apiNameValue,
                        controllerName,
                        httpMethod,
                        uri,
                        messageId,
                        inputType,
                        code,
                        durationMs,
                        clientIp,
                        errorMessage
                );
            } else {
                log.info(
                        "api_access traceId={} apiName={} controller={} httpMethod={} uri={} messageId={} inputType={} code={} durationMs={} clientIp={}",
                        traceId,
                        apiNameValue,
                        controllerName,
                        httpMethod,
                        uri,
                        messageId,
                        inputType,
                        code,
                        durationMs,
                        clientIp
                );
            }

            logDebugBody(requestMessage, result);
        }
    }

    private void logDebugBody(BaseMessage requestMessage, Object result) {
        if (!log.isDebugEnabled()) {
            return;
        }
        if (config.isLogRequestBody() && requestMessage != null && requestMessage.getData() != null) {
            log.debug("api_access_request_body {}", sanitizer.sanitize(requestMessage.getData()));
        }
        if (config.isLogResponseBody() && result instanceof BaseMessage responseMessage
                && responseMessage.getData() != null) {
            log.debug("api_access_response_body {}", sanitizer.sanitize(responseMessage.getData()));
        }
    }

    private static String resolveApiName(ApiName apiName, String controllerName, String methodName) {
        if (apiName != null) {
            String name = apiName.name();
            if (name != null && !name.isBlank()) {
                return name;
            }
            String value = apiName.value();
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return controllerName + "#" + methodName;
    }

    private static String resolveInputType(ApiName apiName) {
        if (apiName == null) {
            return "-";
        }
        Class<?>[] inputTypes = apiName.input();
        if (inputTypes == null || inputTypes.length == 0 || inputTypes[0] == null) {
            return "-";
        }
        return inputTypes[0].getSimpleName();
    }

    private static String resolveCode(Object result, Throwable error) {
        if (error instanceof AppException appException) {
            return appException.getErrorCode();
        }
        if (error != null) {
            return "FAILED";
        }
        if (result instanceof BaseMessage baseMessage && baseMessage.getCode() != null) {
            return baseMessage.getCode();
        }
        return "SUCCESS";
    }

    private static BaseMessage findFirstBaseMessage(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof BaseMessage baseMessage) {
                return baseMessage;
            }
        }
        return null;
    }

    private boolean isExcluded(String uri) {
        if (uri == null) {
            return false;
        }
        List<String> patterns = config.getExcludePathPatterns();
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        for (String pattern : patterns) {
            if (pattern != null && pathMatcher.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    private static HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return servletRequestAttributes.getRequest();
        }
        return null;
    }

    private static String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            if (commaIndex > 0) {
                return forwarded.substring(0, commaIndex).trim();
            }
            return forwarded.trim();
        }
        return request.getRemoteAddr();
    }
}
