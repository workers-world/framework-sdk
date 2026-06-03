package com.books.framework.id_generator;

import com.books.framework.utils.DateUtils;

/**
 * 按天重置的自增 ID 生成器。
 * <p>
 * 格式：{@code PREFIX + yyyyMMdd + 6位序号}，例如 {@code CMB20260520000001}。
 * 跨天自动切换 bizDate，单日上限 999999。
 */
public class IDGenerator {

    static final long MAX_SEQ = 999_999L;

    private final String prefix;
    private final IdSequenceStore store;

    public IDGenerator(String prefix) {
        this(prefix, new InMemoryIdSequenceStore());
    }

    public IDGenerator(String prefix, IdSequenceStore store) {
        this.prefix = prefix;
        this.store = store;
    }

    public String genId() {
        String today = DateUtils.getCurDate();
        long n = store.nextSeq(prefix, today);
        if (n > MAX_SEQ) {
            throw new IllegalStateException(
                    "ID 序号溢出: prefix=" + prefix + ", bizDate=" + today + ", seq=" + n);
        }
        return prefix + today + String.format("%06d", n);
    }
}
