package com.books.framework.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import jakarta.annotation.Nullable;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 从请求头 / 可选 {@link TraceIdContributor} 读取或生成 traceId，写入 MDC 并回写响应头。
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TraceIdFilter extends OncePerRequestFilter {

    @Nullable
    private final TraceIdContributor contributor;

    public TraceIdFilter() {
        this(null);
    }

    public TraceIdFilter(@Nullable TraceIdContributor contributor) {
        this.contributor = contributor;
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
        if (contributor != null) {
            String contributed = contributor.contribute(request);
            if (contributed != null && !contributed.isBlank()) {
                return contributed;
            }
        }
        return TraceIdUtils.generate();
    }
}
