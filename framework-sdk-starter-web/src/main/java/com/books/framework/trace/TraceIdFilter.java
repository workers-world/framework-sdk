package com.books.framework.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从请求头 / Micrometer Span 读取或生成 traceId，写入 MDC 并回写响应头。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceIdFilter extends OncePerRequestFilter {

    @Nullable
    private final Tracer tracer;

    public TraceIdFilter() {
        this(null);
    }

    public TraceIdFilter(@Nullable Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        TraceIdUtils.set(traceId);
        response.setHeader(TraceIdUtils.HEADER_NAME, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TraceIdUtils.clear();
        }
    }

    private String resolveTraceId(HttpServletRequest request) {
        String header = request.getHeader(TraceIdUtils.HEADER_NAME);
        if (header != null && !header.trim().isEmpty()) {
            return header.trim();
        }
        if (tracer != null) {
            Span span = tracer.currentSpan();
            if (span != null && span.context() != null) {
                String traceId = span.context().traceId();
                if (traceId != null && !traceId.isBlank()) {
                    return traceId;
                }
            }
        }
        return TraceIdUtils.generate();
    }
}
