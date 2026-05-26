package com.books.framework.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 字符串工具类
 *
 * @author zero
 * @description 提供字符串相关的常用操作方法
 * @date 2024-11-28
 */
public class StringUtils {

    /**
     * 比较两个字符串是否相等
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 是否相等
     */
    public static boolean eq(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    // 比较是否不等
    public static boolean ne(String str1, String str2) {
        return !eq(str1, str2);
    }

    /**
     * 判断字符串是否为空（null 或空串）。
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否为空白（null、空串、仅空白字符）。
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 待检查的字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否非空白。
     *
     * @param str 待检查字符串
     * @return 是否非空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 截取字符串到指定长度
     *
     * @param str 原字符串
     * @param len 目标长度
     * @return 截取后的字符串
     */
    public static String cut(String str, int len) {
        return org.apache.commons.lang3.StringUtils.left(str, len);
    }

    /**
     * 去掉首尾空白；若结果为空串则返回 null。
     *
     * @param str 原字符串
     * @return 处理后的字符串
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 去掉首尾空白；若入参为 null 返回空串。
     *
     * @param str 原字符串
     * @return 处理后的字符串
     */
    public static String trimToEmpty(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    /**
     * 移除字符串中所有空白字符（空格、制表、换行等）。
     */
    public static String removeWhitespace(String str) {
        if (str == null) return null;
        return str.replaceAll("\\s+", "");
    }

    /**
     * 逗号分隔的字符串拆成 trimmed 的非空分段（常见于多渠道、多 ID 筛选）。
     */
    public static List<String> splitCommaTrimmedNonBlank(String csv) {
        if (isBlank(csv)) {
            return Collections.emptyList();
        }
        String[] split = csv.split(",");
        List<String> result = new ArrayList<>(split.length);
        for (String item : split) {
            if (item == null) {
                continue;
            }
            String value = trimToNull(item);
            if (isNotBlank(value)) {
                result.add(value);
            }
        }
        return result;
    }

    /**
     * 逗号分隔的 Long 分段；非法条目跳过。
     */
    public static List<Long> splitCommaTrimmedToLongs(String csv) {
        if (isBlank(csv)) {
            return Collections.emptyList();
        }
        String[] split = csv.split(",");
        List<Long> result = new ArrayList<>(split.length);
        for (String item : split) {
            if (item == null) {
                continue;
            }
            String value = trimToNull(item);
            if (isBlank(value)) {
                continue;
            }
            try {
                result.add(Long.valueOf(value));
            } catch (NumberFormatException ignored) {
                // skip
            }
        }
        return result;
    }

    /**
     * 供 MySQL {@code LIKE pattern ESCAPE '!'}：转义 {@code !}、{@code %}、{@code _}，避免字面量被当作通配符。
     */
    public static String escapeMysqlLikeBang(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }

    /**
     * Trim 后若为空或与 {@code "ALL"} 忽略大小写相同，则视为「不按该项过滤」（返回 {@code null}）。
     */
    public static String trimOrAllTokenToNull(String raw) {
        String c = trimToNull(raw);
        if (c == null || "ALL".equalsIgnoreCase(c)) {
            return null;
        }
        return c;
    }

    /**
     * 截断到指定长度，超长则取前 maxLen 个字符。
     */
    public static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }

    /**
     * 判断字符串 value 是否不是给定的任意一个候选值
     * 等价于：value != a && value != b && ...
     */
    public static boolean isNoneOf(String value, String... candidates) {
        if (candidates == null || candidates.length == 0) {
            return true;
        }
        for (String c : candidates) {
            // Objects.equals 可安全处理 null
            if (java.util.Objects.equals(value, c)) {
                return false;  // 等于其中之一，就不是“全都不等”
            }
        }
        return true;
    }
}
