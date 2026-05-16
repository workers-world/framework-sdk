package com.books.framework.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DateUtils测试类
 *
 * @author zero
 * @description DateUtils工具类测试
 * @date 2024-12-01
 */
class DateUtilsTest {

    @Test
    void testGetCurDateTime() {
        String dateTime = DateUtils.getCurDateTime();

        // 验证格式：14位数字
        assertNotNull(dateTime);
        assertEquals(14, dateTime.length());
        assertTrue(dateTime.matches("\\d{14}"));
    }

    @Test
    void testGetCurDate() {
        String date = DateUtils.getCurDate();

        // 验证格式：8位数字
        assertNotNull(date);
        assertEquals(8, date.length());
        assertTrue(date.matches("\\d{8}"));
    }

    @Test
    void testGetCurTime() {
        String time = DateUtils.getCurTime();

        // 验证格式：6位数字
        assertNotNull(time);
        assertEquals(6, time.length());
        assertTrue(time.matches("\\d{6}"));
    }

    @Test
    void testGetCurDateTimeWithMillis() {
        String dateTimeWithMillis = DateUtils.getCurDateTimeWithMillis();

        // 验证格式：17位数字
        assertNotNull(dateTimeWithMillis);
        assertEquals(17, dateTimeWithMillis.length());
        assertTrue(dateTimeWithMillis.matches("\\d{17}"));
    }

    @Test
    void testCompareDate() {
        // 测试正常比较
        assertTrue(DateUtils.compareDate("20240101", "20240102") < 0);
        assertTrue(DateUtils.compareDate("20240102", "20240101") > 0);
        assertEquals(0, DateUtils.compareDate("20240101", "20240101"));

        // 测试异常情况
        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.compareDate(null, "20240101");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.compareDate("20240101", null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.compareDate("202401", "20240101");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            DateUtils.compareDate("20240101", "202401");
        });
    }

    @Test
    void testParseDateTimeHyphenFormats() {
        LocalDateTime dt = DateUtils.parseDateTime("2025-10-06 12:59:31");
        assertNotNull(dt);
        assertEquals(2025, dt.getYear());
        assertEquals(10, dt.getMonthValue());
        assertEquals(6, dt.getDayOfMonth());
        assertEquals(12, dt.getHour());
        assertEquals(59, dt.getMinute());
        assertEquals(31, dt.getSecond());

        dt = DateUtils.parseDateTime("2025-10-06 12:59");
        assertNotNull(dt);
        assertEquals(2025, dt.getYear());
        assertEquals(12, dt.getHour());

        dt = DateUtils.parseDateTime("2025-10-06");
        assertNotNull(dt);
        assertEquals(2025, dt.getYear());
        assertEquals(0, dt.getHour());
    }

    @Test
    void testParseDateTimeSlashFormats() {
        LocalDateTime dt = DateUtils.parseDateTime("2025/10/06 12:59:31");
        assertNotNull(dt);
        assertEquals(2025, dt.getYear());
        assertEquals(10, dt.getMonthValue());
        assertEquals(6, dt.getDayOfMonth());
        assertEquals(12, dt.getHour());
        assertEquals(59, dt.getMinute());
        assertEquals(31, dt.getSecond());

        dt = DateUtils.parseDateTime("2025/10/06 12:59");
        assertNotNull(dt);
        assertEquals(2025, dt.getYear());

        dt = DateUtils.parseDateTime("2025/10/06");
        assertNotNull(dt);
        assertEquals(2025, dt.getYear());
        assertEquals(0, dt.getHour());
    }

    @Test
    void testParseDateTimeNullAndInvalid() {
        assertNull(DateUtils.parseDateTime(null));
        assertNull(DateUtils.parseDateTime(""));
        assertNull(DateUtils.parseDateTime("  "));
        assertNull(DateUtils.parseDateTime("not-a-date"));
    }
}
