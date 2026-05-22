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

    public static final DateTimeFormatter STD_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FMT_14 = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter FMT_8 = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter FMT_6 = DateTimeFormatter.ofPattern("HHmmss");
    public static final DateTimeFormatter FMT_17 = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    public static final DateTimeFormatter FMT_TIME = DateTimeFormatter.ofPattern("HH:mm");

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


    private static final DateTimeFormatter[] PARSE_FORMATTERS = {
            DateTimeFormatter.ofPattern("yyyy-M-d H:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-M-d H:mm"),
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/M/d H:mm"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
    };

    public static LocalDateTime parseDateTime(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String clean = s.trim();
        for (int i = 0; i < PARSE_FORMATTERS.length; i++) {
            try {
                DateTimeFormatter fmt = PARSE_FORMATTERS[i];
                if (i == 2 || i == 5) {
                    LocalDate date = LocalDate.parse(clean, fmt);
                    return date.atStartOfDay();
                }
                return LocalDateTime.parse(clean, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        log.error("[{}] 解析日期时间失败，所有格式均不匹配", s);
        return null;
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

    public static boolean isSameDay(LocalDateTime t1, LocalDateTime t2) {
        if (t1 == null || t2 == null) {
            return false;
        }

        return t1.toLocalDate().isEqual(t2.toLocalDate());
    }

    /**
     * 两个日期时间字符串是否同一天。先尝试解析，任一个解析失败返回 false。
     */
    public static boolean isSameDay(String t1, String t2) {
        if (t1 == null || t2 == null) return false;
        LocalDateTime dt1 = tryParseDateTime(t1);
        LocalDateTime dt2 = tryParseDateTime(t2);
        return dt1 != null && dt2 != null && isSameDay(dt1, dt2);
    }

    /**
     * 两个日期时间字符串的绝对差是否在指定小时数内。
     */
    public static boolean timeWithinHours(String t1, String t2, int hours) {
        if (t1 == null || t2 == null) return false;
        LocalDateTime dt1 = tryParseDateTime(t1);
        LocalDateTime dt2 = tryParseDateTime(t2);
        if (dt1 == null || dt2 == null) return false;
        return Math.abs(java.time.temporal.ChronoUnit.HOURS.between(dt1, dt2)) <= hours;
    }

    /**
     * 两个日期时间字符串之间的绝对小时差（含分钟精度）。解析失败返回 -1。
     */
    public static double timeDiffHours(String t1, String t2) {
        if (t1 == null || t2 == null) return -1;
        LocalDateTime dt1 = tryParseDateTime(t1);
        LocalDateTime dt2 = tryParseDateTime(t2);
        if (dt1 == null || dt2 == null) return -1;
        return Math.abs(ChronoUnit.MINUTES.between(dt1, dt2)) / 60.0;
    }

    /**
     * 尝试从字符串中解析日期时间，成功则返回，失败返回 null。
     */
    public static LocalDateTime tryParseDateTime(String s) {
        if (s == null || s.isBlank()) return null;
        String clean = s.trim();
        for (DateTimeFormatter fmt : PARSE_FORMATTERS) {
            if (fmt == FMT_6 || fmt == FMT_TIME) continue;
            try { return LocalDateTime.parse(clean, fmt); } catch (DateTimeParseException ignored) {}
            try { return LocalDate.parse(clean, fmt).atStartOfDay(); } catch (DateTimeParseException ignored) {}
        }
        return null;
    }

     public static String normalizeDateTime(String raw) {
        if (raw == null || raw.isBlank()) return "";
        LocalDateTime dt = DateUtils.tryParseDateTime(raw);
        return dt != null ? dt.format(STD_DATE_FMT) : raw;
    }
}
