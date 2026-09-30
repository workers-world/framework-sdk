package com.books.framework.utils;

import java.math.BigDecimal;

/**
 * 数值解析工具（与 {@link StringUtils#trimToNull(String)} 配合的空安全解析）。
 */
public final class NumberUtils {

    private NumberUtils() {
    }

    /**
     * Trim 后为空则 {@code null}，否则 {@link BigDecimal} 解析（非法格式抛出 {@link NumberFormatException}）。
     */
    public static BigDecimal parseBigDecimalOrNullIfBlank(String raw) {
        String s = StringUtils.trimToNull(raw);
        if (s == null) {
            return null;
        }
        return new BigDecimal(s);
    }
}
