package com.books.framework.utils;

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

    /**
     * 判断字符串是否为空
     *
     * @param str 待检查的字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }

        if (StringUtils.eq("", str.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 判断字符串是否为空白（null、空串、仅空白字符）。
     *
     * @param str 待检查字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return isEmpty(str);
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
}
