package com.books.framework.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * StringUtils测试类
 *
 * @author zero
 * @description StringUtils工具类测试
 * @date 2024-12-01
 */
class StringUtilsTest {

    @Test
    void testEq() {
        // 测试相等的情况
        assertTrue(StringUtils.eq("hello", "hello"));
        assertTrue(StringUtils.eq("", ""));

        // 测试不相等的情况
        assertFalse(StringUtils.eq("hello", "world"));
        assertFalse(StringUtils.eq("hello", ""));

        // 测试null的情况
        assertFalse(StringUtils.eq(null, "hello"));
        assertFalse(StringUtils.eq("hello", null));
        assertFalse(StringUtils.eq(null, null));
    }

    @Test
    void testIsEmpty() {
        // 测试空字符串
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
        assertTrue(StringUtils.isEmpty("   "));
        assertTrue(StringUtils.isEmpty("\t\n"));

        // 测试非空字符串
        assertFalse(StringUtils.isEmpty("hello"));
        assertFalse(StringUtils.isEmpty(" hello "));
    }

    @Test
    void testIsNotEmpty() {
        // 测试非空字符串
        assertTrue(StringUtils.isNotEmpty("hello"));
        assertTrue(StringUtils.isNotEmpty(" hello "));

        // 测试空字符串
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
        assertFalse(StringUtils.isNotEmpty("   "));
    }

    @Test
    void testCut() {
        // 测试正常截取
        assertEquals("Hello", StringUtils.cut("Hello World", 5));
        assertEquals("Hello", StringUtils.cut("Hello", 5));

        // 测试截取长度大于原字符串长度
        assertEquals("Hello World", StringUtils.cut("Hello World", 20));

        // 测试空字符串
        assertEquals("", StringUtils.cut("", 5));

        // 测试中文字符
        assertEquals("我", StringUtils.cut("我我我我我", 1));
        assertEquals("我我", StringUtils.cut("我我我我我", 2));
    }

    @Test
    void testIsBlankAndIsNotBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertTrue(StringUtils.isBlank("\t\n"));

        assertFalse(StringUtils.isBlank("a"));
        assertFalse(StringUtils.isBlank(" a "));

        assertFalse(StringUtils.isNotBlank(null));
        assertFalse(StringUtils.isNotBlank("   "));
        assertTrue(StringUtils.isNotBlank("abc"));
    }

    @Test
    void testTrimToNull() {
        assertNull(StringUtils.trimToNull(null));
        assertNull(StringUtils.trimToNull(""));
        assertNull(StringUtils.trimToNull("   "));
        assertEquals("abc", StringUtils.trimToNull("  abc  "));
    }

    @Test
    void testTrimToEmpty() {
        assertEquals("", StringUtils.trimToEmpty(null));
        assertEquals("", StringUtils.trimToEmpty(""));
        assertEquals("", StringUtils.trimToEmpty("   "));
        assertEquals("abc", StringUtils.trimToEmpty("  abc  "));
    }
}
