package com.books.framework.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.Nullable;

/**
 * 从 Micrometer {@link Tracer} 当前 Span 读取 traceId；由 Micrometer 自动配置按需注册。
 */
public class MicrometerTraceIdContributor implements TraceIdContributor {

    @Nullable
    private final Tracer tracer;

    public MicrometerTraceIdContributor(@Nullable Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    @Nullable
    public String contribute(HttpServletRequest request) {
        if (tracer == null) {
            return null;
        }
        Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        String traceId = span.context().traceId();
        return traceId.isBlank() ? null : traceId;
    }
}
