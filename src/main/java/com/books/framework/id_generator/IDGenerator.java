package com.books.framework.id_generator;

import com.books.framework.utils.DateUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 按天重置的自增 ID 生成器。
 * <p>
 * 格式：{@code PREFIX + yyyyMMdd + 6位序号}，例如 {@code CMB20260520000001}。
 * 跨天自动重置序号，单日上限 999999。
 */
public class IDGenerator {

    private static final long MAX_SEQ = 999_999L;

    private final String prefix;
    private volatile String curDay;
    private final AtomicLong seq;

    public IDGenerator(String prefix) {
        this.prefix = prefix;
        this.curDay = DateUtils.getCurDate();
        this.seq = new AtomicLong(0);
    }

    public String genId() {
        rotateDayIfNeeded();
        long n = seq.incrementAndGet();
        if (n > MAX_SEQ) {
            throw new IllegalStateException(
                    "ID 序号溢出: prefix=" + prefix + ", curDay=" + curDay + ", seq=" + n);
        }
        return prefix + curDay + String.format("%06d", n);
    }

    private void rotateDayIfNeeded() {
        String today = DateUtils.getCurDate();
        if (!today.equals(curDay)) {
            synchronized (this) {
                today = DateUtils.getCurDate();
                if (!today.equals(curDay)) {
                    curDay = today;
                    seq.set(0);
                }
            }
        }
    }
}
