package com.books.framework.trace;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * TraceId 工具——写入/读取 MDC，生成短 traceId。
 */
public final class TraceIdUtils {

    public static final String MDC_KEY = "traceId";
    public static final String HEADER_NAME = "X-Trace-Id";

    private TraceIdUtils() {
    }

    /**
     * 生成 16 位 hex 短 traceId（时间戳 + 随机数）。
     */
    public static String generate() {
        long time = System.currentTimeMillis();
        int rand = ThreadLocalRandom.current().nextInt(0xFFFF);
        return String.format("%011x%04x", time, rand);
    }

    /**
     * 生成标准 UUID（不带横线）。
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String get() {
        return MDC.get(MDC_KEY);
    }

    public static void set(String traceId) {
        MDC.put(MDC_KEY, traceId);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }

    public static String getOrGenerate() {
        String tid = get();
        if (tid == null) {
            tid = generate();
            set(tid);
        }
        return tid;
    }
}
