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
        // 先尝试直接解析（兼容科学计数法如 1E+30），BigDecimal 原生支持
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException ignored) {
        }
        // 否则做清洗（去符号、空格等）后再解析
        String cleaned = trimmed.replaceAll("[^\\d.\\-Ee]", "");
        if (cleaned.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("金额解析失败, raw={}", raw, e);
            throw e;
        }
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
