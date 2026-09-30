package com.books.framework.redis;

import com.books.framework.config.FrameworkProperties;
import com.books.framework.idempotent.IdempotentStorage;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * 基于 Redis SET NX 的分布式幂等存储。
 */
public class RedisIdempotentStorage implements IdempotentStorage {

    private final StringRedisTemplate redis;
    private final String keyPrefix;

    public RedisIdempotentStorage(StringRedisTemplate redis, FrameworkProperties properties) {
        this.redis = redis;
        this.keyPrefix = properties.getRedis().getKeyPrefix() + "idempotent:";
    }

    @Override
    public boolean tryAcquire(String key, long ttlSeconds) {
        if (key == null || key.isBlank()) {
            return false;
        }
        String redisKey = keyPrefix + key;
        Boolean ok = redis.opsForValue().setIfAbsent(redisKey, "1", Duration.ofSeconds(Math.max(1, ttlSeconds)));
        return Boolean.TRUE.equals(ok);
    }

    @Override
    public void release(String key) {
        if (key != null && !key.isBlank()) {
            redis.delete(keyPrefix + key);
        }
    }
}
