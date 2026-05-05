package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 日期工具类
 *
 * @author zero
 * @description 提供日期时间相关的常用操作方法
 * @date 2024-11-28
 */
@Slf4j
public class DateUtils {

    private static final DateTimeFormatter FMT_14 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter FMT_8 = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter FMT_6 = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter FMT_17 = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final DateTimeFormatter FMT_TIME = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 获取14位时间字符串，格式：yyyyMMddHHmmss
     */
    public static String getCurDateTime() {
        return FMT_14.format(LocalDateTime.now());
    }

    /**
     * 获取8位日期字符串，格式：yyyyMMdd
     */
    public static String getCurDate() {
        return FMT_8.format(LocalDate.now());
    }

    /**
     * 获取6位时间字符串，格式：HHmmss
     */
    public static String getCurTime() {
        return FMT_6.format(LocalTime.now());
    }

    /**
     * 获取17位时间字符串（含毫秒），格式：yyyyMMddHHmmssSSS
     */
    public static String getCurDateTimeWithMillis() {
        return FMT_17.format(LocalDateTime.now());
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

    /**
     * 解析 ISO-8601 日期字符串（{@code yyyy-MM-dd}）；失败时返回兜底日期。
     */
    public static LocalDate parseIsoDateOrFallback(String text, LocalDate fallback) {
        try {
            return LocalDate.parse(text);
        } catch (Exception e) {
            return fallback;
        }
    }

    /**
     * 含首尾：从 {@code start} 起到 {@code end}（含）共多少个月。
     */
    public static long countMonthsInclusive(YearMonth start, YearMonth end) {
        return ChronoUnit.MONTHS.between(start, end) + 1;
    }

    /**
     * 上一年同月的 {@code yyyy-MM} 字符串（用于同比上月键）。
     */
    public static String priorCalendarYearMonth(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        return YearMonth.from(ym.atDay(1).minusYears(1)).toString();
    }

    /**
     * 从左到右、含首尾月份的 {@code yyyy-MM} 标签列表。
     */
    public static List<String> yearMonthStringsInclusive(YearMonth ymStart, YearMonth ymEnd) {
        List<String> out = new ArrayList<>();
        for (YearMonth cur = ymStart; !cur.isAfter(ymEnd); cur = cur.plusMonths(1)) {
            out.add(cur.toString());
        }
        return out;
    }

    /**
     * 看板按日汇总查询：[startTime,endTime] 为自然日闭区间（首日 00:00 至末日 23:59:59.999999999），{@code days} 为跨越的自然日个数（首尾均算）。
     * <p>
     * 规则：起止均为空则默认本月 1 日至今天；仅空开始则开始=结束日；仅空结束则结束=今天；若开始晚于结束则互换。
     */
    public static QueryDayRange resolveAnalyticsQueryDayRange(String startDate, String endDate) {
        return resolveAnalyticsQueryDayRange(startDate, endDate, LocalDate.now());
    }

    /**
     * 同 {@link #resolveAnalyticsQueryDayRange(String, String)}，可注入「今天」便于单测。
     */
    public static QueryDayRange resolveAnalyticsQueryDayRange(String startDate, String endDate, LocalDate today) {
        LocalDate start;
        LocalDate end;
        if (StringUtils.isBlank(startDate) && StringUtils.isBlank(endDate)) {
            start = today.withDayOfMonth(1);
            end = today;
        } else if (StringUtils.isBlank(startDate)) {
            end = parseIsoDateOrFallback(endDate, today);
            start = end;
        } else if (StringUtils.isBlank(endDate)) {
            start = parseIsoDateOrFallback(startDate, today.withDayOfMonth(1));
            end = today;
        } else {
            start = parseIsoDateOrFallback(startDate, today.withDayOfMonth(1));
            end = parseIsoDateOrFallback(endDate, today);
        }
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }
        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);
        int days = (int) ChronoUnit.DAYS.between(start, end) + 1;
        return new QueryDayRange(startTime, endTime, days);
    }

    /**
     * {@link #resolveAnalyticsQueryDayRange(String, String)} 的返回值。
     */
    public static final class QueryDayRange {
        public final LocalDateTime startTime;
        public final LocalDateTime endTime;
        public final int days;

        QueryDayRange(LocalDateTime startTime, LocalDateTime endTime, int days) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.days = days;
        }
    }

    /**
     * 将 {@link LocalDateTime#toString()} 形式中的 {@code T} 替换为空格，便于与仅支持 {@code yyyy-MM-dd HH:mm:ss} 的展示层对齐。
     */
    public static String formatLocalDateTimeWithSpace(LocalDateTime t) {
        if (t == null) {
            return null;
        }
        return t.toString().replace('T', ' ');
    }

    /**
     * Trim 后为空则 {@code null}；否则解析 ISO-8601 日期（{@code yyyy-MM-dd}）。
     *
     * @throws DateTimeParseException 非空且格式非法
     */
    public static LocalDate parseIsoLocalDateOrNullIfBlank(String value) {
        String s = StringUtils.trimToNull(value);
        if (s == null) {
            return null;
        }
        return LocalDate.parse(s);
    }

    /**
     * 两边界均可为空的按日查询：均为空则起止时刻均为 {@code null}（表示不按日过滤）；单侧空时与「场景」等业务约定自行见方法体注释。
     *
     * @param today 仅当「结束日缺省」时使用，表示过滤到含该日为止
     */
    public static OptionalDayTimeRange buildScenarioOptionalDayTimeRange(
            LocalDate startDate, LocalDate endDate, LocalDate today) {
        if (startDate == null && endDate == null) {
            return new OptionalDayTimeRange(null, null);
        }
        LocalDate s = startDate;
        LocalDate e = endDate;
        if (s == null) {
            s = e;
        }
        if (e == null) {
            e = today;
        }
        if (s.isAfter(e)) {
            LocalDate tmp = s;
            s = e;
            e = tmp;
        }
        return new OptionalDayTimeRange(s.atStartOfDay(), e.atTime(LocalTime.MAX));
    }

    /**
     * {@link #buildScenarioOptionalDayTimeRange(LocalDate, LocalDate, LocalDate)} 的返回值；起止均可为 {@code null} 表示不过滤。
     */
    public static final class OptionalDayTimeRange {
        public final LocalDateTime startTime;
        public final LocalDateTime endTime;

        OptionalDayTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }


    public static LocalDateTime parseDateTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            // 如果数据中有时带秒、有时不带
            // 可以用 DateTimeFormatterBuilder 把秒作为可选部分
            DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-M-d H:mm")
                    .optionalStart()
                    .appendPattern(":ss")
                    .optionalEnd()
                    .toFormatter();

            return LocalDateTime.parse(s.trim(), fmt);
        } catch (Exception e) {
            log.error("[{}] 解析日期时间失败, {}", s, e);
            return null;
        }
    }

    // 提取 HH:mm
    public static LocalTime parseTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            // 获取字符串格式的 HH:mm
            String time = s.trim();
            if (s.length() != 5) {
                LocalDateTime dateTime = parseDateTime(s);
                time = dateTime.format(FMT_TIME);
            }

            // 转换为LocalTime
            LocalTime t1 = LocalTime.parse(time, FMT_TIME);
            return t1;
        } catch (Exception e) {
            log.error("[{}] 解析时间失败, {}", s, e);
            return null;
        }
    }
}
