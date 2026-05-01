package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/**
 * @author zero
 * @description todo
 * @date 2026-05-01
 */
@Slf4j
public class AmountUtil {

    public static BigDecimal parseAmount(String raw) {
        String cleaned = cleanAmount(raw);
        if (cleaned == null) {
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
        String cleaned = cleanAmount(raw);
        if (cleaned == null) {
            return false;
        }
        try {
            new BigDecimal(cleaned);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String cleanAmount(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        return raw.trim().replaceAll("[^\\d.\\-]", "");
    }
}
