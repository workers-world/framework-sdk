package com.books.framework.trace;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.Nullable;

/**
 * 可选的 traceId 来源扩展（如 Micrometer Span），由自动配置按需注册。
 */
@FunctionalInterface
public interface TraceIdContributor {

    /**
     * @return 非空的 traceId，或 null 表示本贡献者无法提供
     */
    @Nullable
    String contribute(HttpServletRequest request);
}
