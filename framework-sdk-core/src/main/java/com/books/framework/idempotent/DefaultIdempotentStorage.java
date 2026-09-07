package com.books.framework.idempotent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认内存存储——基于 {@link ConcurrentHashMap} + 时间戳比对。
 * <p>一次性 key 此前永不清理导致无界增长;现于条目数超过阈值时顺手清扫过期项。</p>
 */
public class DefaultIdempotentStorage implements IdempotentStorage {

    /** 触发清扫的条目数阈值 */
    private static final int SWEEP_THRESHOLD = 1024;

    private final ConcurrentHashMap<String, Long> store = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, long ttlSeconds) {
        long now = System.currentTimeMillis();
        if (store.size() >= SWEEP_THRESHOLD) {
            sweepExpired(now);
        }
        Long existing = store.putIfAbsent(key, now);
        if (existing == null) {
            return true;
        }
        // key 已存在，检查是否过期
        long expireAt = existing + ttlSeconds * 1000;
        if (now >= expireAt) {
            // 过期则覆盖（原子替换）
            return store.replace(key, existing, now);
        }
        return false;
    }

    @Override
    public void release(String key) {
        store.remove(key);
    }

    private void sweepExpired(long now) {
        // 过期与否取决于各 key 自身的 ttl,这里按保守策略:入库超过 24h 的一律清除
        long staleBefore = now - 24 * 3600 * 1000L;
        store.values().removeIf(acquiredAt -> acquiredAt < staleBefore);
    }
}
