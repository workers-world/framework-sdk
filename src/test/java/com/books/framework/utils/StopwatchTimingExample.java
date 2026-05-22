package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.util.concurrent.TimeUnit;

/**
 * {@link StopwatchTiming} 使用示例，可直接运行 main 方法查看输出。
 *
 * <h3>核心概念</h3>
 * <ul>
 *   <li>{@link StopWatch} 是 Spring 内置的秒表，每个任务独立计时，不支持嵌套/交错，需串行
 *       start → stop → start → stop</li>
 *   <li>{@link StopwatchTiming} 封装了格式化输出：统一以毫秒为单位，提供单行摘要和表格两种风格</li>
 *   <li>使用 {@code [timing:xxx]} 前缀便于在日志平台按关键字检索，统计 P50/P99 耗时</li>
 * </ul>
 *
 * <h3>典型写法</h3>
 * <pre>{@code
 * StopWatch watch = new StopWatch("业务标识");
 * watch.start("阶段A");
 * doA();
 * watch.stop();
 * watch.start("阶段B");
 * doB();
 * watch.stop();
 * StopwatchTiming.logInfo(log, "biz.key", watch);
 * }</pre>
 *
 * <h3>使用场景</h3>
 * 适用于 Java 应用中的关键链路耗时分析：定时任务、接口性能、批处理流水线等。不适用于微秒级精细化打点——此时应考虑 Micrometer / Prometheus。
 *
 * @author zero
 * @see StopwatchTiming
 * @see StopWatch
 */
@Slf4j
public class StopwatchTimingExample {

    public static void main(String[] args) {
        new StopwatchTimingExample().runAll();
    }

    void runAll() {
        demoBasic();
        demoPrettyPrint();
        demoLogInfo();
        demoWithException();
        demoCompactSummary();
    }

    // ========================================================================
    // 示例 1：基础用法 — 多个阶段串行计时
    // ========================================================================

    /**
     * 基础用法：三个步骤串行计时，最终一行输出所有任务耗时。
     *
     * <pre>
     * 日志输出：
     * [timing:example.basic] totalMs=1234, StopWatch 'basic-demo': tasks=[step-db=950ms, step-cpu=200ms, step-io=84ms]
     * </pre>
     */
    void demoBasic() {
        StopWatch watch = new StopWatch("basic-demo");

        // 阶段1：模拟数据库查询
        watch.start("step-db");
        sleepMs(200);
        watch.stop();

        // 阶段2：模拟 CPU 计算
        watch.start("step-cpu");
        sleepMs(50);
        watch.stop();

        // 阶段3：模拟外部接口调用
        watch.start("step-io");
        sleepMs(80);
        // 故意不 stop，演示 logInfo 内部自动兜底

        // logInfo 内部检测到 isRunning() 自动补 stop，所以即使 step-io 没 stop 也会被记录
        // 关键点：不能连续两次 start 不 stop，否则抛 AlreadyRunningException
        StopwatchTiming.logInfo(log, "example.basic", watch);
    }

    // ========================================================================
    // 示例 2：表格输出 — 多任务带百分比，适合 DEBUG 级别
    // ========================================================================

    /**
     * 表格输出：当需要详细耗时占比时使用，通常在 DEBUG 日志中输出。
     *
     * <pre>
     * StopWatch 'complex-demo': running time = 6505 ms
     * -------------------------------------------
     *      ms     %     Task name
     * -------------------------------------------
     *    2000  30.7%  step-db
     *    3500  53.8%  step-io
     *    1005  15.4%  step-calculate
     * -------------------------------------------
     * </pre>
     */
    void demoPrettyPrint() {
        StopWatch watch = new StopWatch("complex-demo");

        watch.start("step-db");
        sleepMs(2000);
        watch.stop();

        watch.start("step-io");
        sleepMs(3500);
        watch.stop();

        watch.start("step-calculate");
        sleepMs(1005);
        watch.stop();

        // prettyPrintMs 只返回表格字符串，不记日志。如果调用方想自己控制日志输出，用这个方法
        String table = StopwatchTiming.prettyPrintMs(watch);
        System.out.println(table);

        // logInfo 内部集成：INFO 级别输出 compact 摘要，DEBUG 级别自动追加表格
        StopwatchTiming.logInfo(log, "example.pretty", watch);
    }

    // ========================================================================
    // 示例 3：实战 — 对账/批处理流水线
    // ========================================================================

    /**
     * 对账流水线示例：模拟查询 → 匹配 → 写入三个阶段。
     *
     * <pre>
     * 日志输出：
     * [timing:reconciliation.execute] totalMs=850, StopWatch 'reco-exec': tasks=[query=300ms, match=450ms, persist=100ms]
     * </pre>
     */
    void demoLogInfo() {
        StopWatch watch = new StopWatch("reco-exec");

        // 查询阶段
        watch.start("query");
        sleepMs(300);
        watch.stop();

        // 匹配阶段 — 占 53%，是优化重点
        watch.start("match");
        sleepMs(450);
        watch.stop();

        // 写入阶段
        watch.start("persist");
        sleepMs(100);
        watch.stop();

        // [timing:xxx] 前缀在 ELK/Splunk 中可统一检索，聚合各阶段 P50/P99
        StopwatchTiming.logInfo(log, "reconciliation.execute", watch);
    }

    // ========================================================================
    // 示例 4：异常场景 — 中途抛异常，计时数据不丢失
    // ========================================================================

    /**
     * 异常场景：即使中途抛异常，logInfo 内部会自动 stop 未完成的任务并输出。
     *
     * <pre>
     * 日志输出：
     * [timing:example.error] totalMs=150, StopWatch 'error-demo': tasks=[ok-step=100ms, fail-step=50ms]
     * </pre>
     */
    void demoWithException() {
        StopWatch watch = new StopWatch("error-demo");

        watch.start("ok-step");
        sleepMs(100);
        watch.stop();

        try {
            watch.start("fail-step");
            sleepMs(50);
            throw new RuntimeException("模拟业务异常");
        } catch (RuntimeException e) {
            // watch 当前 fail-step 处于 running 状态，但不需要手动 stop
            // logInfo 在 finally 中执行，内部 isRunning() 检测为 true 会自动补 stop
        } finally {
            StopwatchTiming.logInfo(log, "example.error", watch);
        }
    }

    // ========================================================================
    // 示例 5：compactTasksSummaryMs — 只取摘要字符串，不写日志
    // ========================================================================

    /**
     * 只取摘要字符串，不写日志。适合在返回值/异常消息中附带耗时信息。
     *
     * <pre>
     * 返回字符串：
     * tasks=[step-a=200ms, step-b=350ms]
     * </pre>
     */
    void demoCompactSummary() {
        StopWatch watch = new StopWatch("summary-demo");

        watch.start("step-a");
        sleepMs(200);
        watch.stop();

        watch.start("step-b");
        sleepMs(350);
        watch.stop();

        // compactTasksSummaryMs 只返回字符串，不写日志
        String summary = StopwatchTiming.compactTasksSummaryMs(watch);
        System.out.println("耗时摘要: " + summary);

        // 典型用途：抛异常时带上耗时信息
        // throw new AppException(ErrorCodeEnum.TIMEOUT, "处理超时, " + summary);
    }

    private static void sleepMs(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
