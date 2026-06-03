package com.books.framework.redis;

import com.books.framework.config.FrameworkProperties;
import com.books.framework.progress.ProgressSnapshot;
import com.books.framework.progress.ProgressStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Optional;

/**
 * 基于 Redis 的进度快照存储（多实例部署可用）。
 */
public class RedisProgressStore implements ProgressStore {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final String keyPrefix;
    private final long ttlSeconds;

    public RedisProgressStore(StringRedisTemplate redis,
                              ObjectMapper objectMapper,
                              FrameworkProperties properties) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.keyPrefix = properties.getRedis().getKeyPrefix() + "progress:";
        this.ttlSeconds = Math.max(60, properties.getProgress().getRedisTtlSeconds());
    }

    @Override
    public void save(ProgressSnapshot snapshot) {
        if (snapshot == null || snapshot.getTaskId() == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            redis.opsForValue().set(keyPrefix + snapshot.getTaskId(), json, Duration.ofSeconds(ttlSeconds));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("进度快照序列化失败: " + snapshot.getTaskId(), e);
        }
    }

    @Override
    public Optional<ProgressSnapshot> get(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return Optional.empty();
        }
        String json = redis.opsForValue().get(keyPrefix + taskId);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, ProgressSnapshot.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("进度快照反序列化失败: " + taskId, e);
        }
    }
}
