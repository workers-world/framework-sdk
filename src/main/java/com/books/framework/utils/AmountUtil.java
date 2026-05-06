package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class AmountUtil {

    public static BigDecimal parseAmount(String raw) {
        String trimmed = raw == null ? null : raw.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // 先尝试直接解析（兼容科学计数法如 1.234E15），BigDecimal 原生支持
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException ignored) {
        }
        // 否则做清洗（去货币符号、空格、逗号等）后再解析
        String cleaned = trimmed.replaceAll("[^\\d.\\-Ee]", "");
        if (cleaned.isEmpty()) {
            throw new NumberFormatException("金额解析失败: " + raw);
        }
        return new BigDecimal(cleaned);
    }

    public static boolean isValidAmount(String raw) {
        try {
            parseAmount(raw);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
