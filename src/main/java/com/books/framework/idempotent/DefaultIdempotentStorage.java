package com.books.framework.idempotent;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认内存存储——基于 {@link ConcurrentHashMap} + 时间戳比对。
 */
public class DefaultIdempotentStorage implements IdempotentStorage {

    private final ConcurrentHashMap<String, Long> store = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, long ttlSeconds) {
        long now = System.currentTimeMillis();
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
}
