package com.books.framework.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author zero
 * @description 提供日期时间相关的常用操作方法
 * @date 2024-11-28
 */
public class DateUtils {

    /**
     * 获取14位时间字符串
     *
     * @return 格式：20241202220300
     */
    public static String getCurDateTime() {
        SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyyMMddHHmmss");
        return yyyyMMddHHmmss.format(new Date());
    }

    /**
     * 获取8位日期字符串
     *
     * @return 格式：20241202
     */
    public static String getCurDate() {
        SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
        return yyyyMMdd.format(new Date());
    }

    /**
     * 获取6位时间字符串
     *
     * @return 格式：220300
     */
    public static String getCurTime() {
        SimpleDateFormat HHmmss = new SimpleDateFormat("HHmmss");
        return HHmmss.format(new Date());
    }

    /**
     * 获取26位时间字符串（包含毫秒）
     *
     * @return 格式：20241202220300123
     */
    public static String getCurDateTimeWithMillis() {
        SimpleDateFormat yyyyMMddHHmmssSSS = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return yyyyMMddHHmmssSSS.format(new Date());
    }

    /**
     * 比较两个8位日期的前后顺序
     *
     * @param date1 日期1 (格式：yyyyMMdd)
     * @param date2 日期2 (格式：yyyyMMdd)
     * @return 如果date1在date2之前返回负数，相等返回0，之后返回正数
     */
    public static int compareDate(String date1, String date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("日期参数不能为空");
        }
        if (date1.length() != 8 || date2.length() != 8) {
            throw new IllegalArgumentException("日期格式必须为8位数字(yyyyMMdd)");
        }
        return date1.compareTo(date2);
    }
}
