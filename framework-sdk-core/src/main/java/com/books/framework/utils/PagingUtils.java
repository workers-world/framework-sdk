package com.books.framework.utils;

/**
 * 分页参数纠偏：页码、每页条数、offset。
 */
public final class PagingUtils {

    private PagingUtils() {
    }

    /**
     * 页码至少为 {@code minPage}；{@code null} 或小于 min 时用 {@code minPage}（常见 min=1）。
     */
    public static int clampPage(Integer page, int minPage) {
        if (page == null || page < minPage) {
            return minPage;
        }
        return page;
    }

    /**
     * {@link #clampPage(Integer, int)}，最小页码为 1。
     */
    public static int clampPage(Integer page) {
        return clampPage(page, 1);
    }

    /**
     * 原始页码 {@code page <= 0} 时用默认值（如列表接口）。
     */
    public static int clampPositivePage(int page, int defaultPage) {
        if (page <= 0) {
            return defaultPage;
        }
        return page;
    }

    /**
     * {@code null}：{@code defaultLimit}；否则限制在 [{@code 1}, {@code maxLimit}]（看板等「未传则用默认」场景）。
     */
    public static int clampLimit(Integer limit, int defaultLimit, int maxLimit) {
        if (limit == null) {
            return defaultLimit;
        }
        return Math.max(1, Math.min(limit, maxLimit));
    }

    /**
     * {@code null} 或小于 1：{@code defaultLimit}；否则 cap 至 {@code maxLimit}（不传或非法时用默认一页条数）。
     */
    public static int clampLimitOrDefaultWhenInvalid(Integer limit, int defaultLimit, int maxLimit) {
        if (limit == null || limit < 1) {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
    }

    /**
     * 原始 int 类型的条数：≤0 用默认值，否则顶到 max（表单直接传 int 时的纠偏）。
     */
    public static int clampPositiveLimitOrDefault(int limit, int defaultLimit, int maxLimit) {
        if (limit <= 0) {
            return defaultLimit;
        }
        return Math.min(limit, maxLimit);
    }

    public static int offset(int page, int limit) {
        return (page - 1) * limit;
    }
}
