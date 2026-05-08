package com.books.framework.utils;

import org.slf4j.Logger;
import org.springframework.lang.Nullable;
import org.springframework.util.StopWatch;

import java.util.Locale;

/**
 * 关键链路耗时上报：Spring {@link StopWatch} 分任务统计，日志中统一前缀便于检索与规则统计。
 * 不使用 {@link StopWatch#shortSummary()}/{@link StopWatch#prettyPrint()}（默认单位为 ns），
 * 均以毫秒输出，便于肉眼判读。
 */
public final class StopwatchTiming {

    private StopwatchTiming() {
    }

    /**
     * 单行任务明细，形如 {@code tasks=[a=1ms, b=2ms]}；无任务时返回 {@code no tasks for 'id'}。
     */
    public static String compactTasksSummaryMs(@Nullable StopWatch watch) {
        if (watch == null) {
            return "";
        }
        return buildStopWatchSummaryLine(false, watch);
    }

    /**
     * DEBUG 下多行输出，单位为 ms（含各任务耗时占比）。
     */
    public static String prettyPrintMs(@Nullable StopWatch watch) {
        if (watch == null || watch.getTaskCount() <= 0) {
            return watch == null ? "" : ("StopWatch '" + watch.getId() + "': no tasks recorded");
        }
        long totalMs = watch.getTotalTimeMillis();
        StringBuilder sb = new StringBuilder(128);
        sb.append("StopWatch '").append(watch.getId()).append("': running time = ").append(totalMs).append(" ms");
        sb.append("\n-------------------------------------------\n");
        sb.append("     ms     %     Task name\n");
        sb.append("-------------------------------------------\n");
        for (StopWatch.TaskInfo task : watch.getTaskInfo()) {
            long tMs = task.getTimeMillis();
            double pct = totalMs > 0 ? (100.0d * (double) tMs / (double) totalMs) : 0.0d;
            sb.append(String.format(Locale.ROOT, "%7d  %5.1f%%  %s%n", tMs, pct, task.getTaskName()));
        }
        sb.append("-------------------------------------------\n");
        return sb.toString();
    }

    /**
     * 若当前尚有未 stop 的任务，先结束再输出（避免遗漏导致 isRunning）。
     *
     * @param operationKey 业务键，建议使用稳定前缀便于日志汇总，例如 {@code analytics.summary}
     */
    public static void logInfo(Logger log, String operationKey, @Nullable StopWatch watch) {
        if (log == null || !log.isInfoEnabled()) {
            return;
        }
        if (watch == null) {
            return;
        }
        if (watch.isRunning()) {
            watch.stop();
        }
        long totalMs = watch.getTotalTimeMillis();
        log.info("[timing:{}] totalMs={}, {}", operationKey, totalMs, buildStopWatchSummaryLine(true, watch));
        if (log.isDebugEnabled()) {
            log.debug("[timing:{}]\n{}", operationKey, prettyPrintMs(watch));
        }
    }

    private static String buildStopWatchSummaryLine(boolean prependStopWatchPrefix, StopWatch watch) {
        String idPart = "'" + watch.getId() + "'";
        if (watch.getTaskCount() <= 0) {
            return prependStopWatchPrefix
                    ? ("StopWatch " + idPart + ": no tasks recorded")
                    : ("no tasks for " + idPart);
        }
        StringBuilder sb = new StringBuilder(96);
        if (prependStopWatchPrefix) {
            sb.append("StopWatch ").append(idPart).append(": ");
        }
        sb.append("tasks=[");
        StopWatch.TaskInfo[] tasks = watch.getTaskInfo();
        for (int i = 0; i < tasks.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            StopWatch.TaskInfo task = tasks[i];
            sb.append(task.getTaskName()).append('=').append(task.getTimeMillis()).append("ms");
        }
        sb.append("]");
        return sb.toString();
    }
}
