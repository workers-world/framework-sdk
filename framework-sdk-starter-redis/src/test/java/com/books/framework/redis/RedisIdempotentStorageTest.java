package com.books.framework.redis;

import com.books.framework.config.FrameworkProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisIdempotentStorageTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisIdempotentStorage storage;

    @BeforeEach
    void setUp() {
        FrameworkProperties properties = new FrameworkProperties();
        properties.getRedis().setKeyPrefix("test:");
        storage = new RedisIdempotentStorage(redisTemplate, properties);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void tryAcquireUsesSetIfAbsentWithPrefix() {
        when(valueOperations.setIfAbsent(eq("test:idempotent:order-1"), eq("1"), any(Duration.class)))
                .thenReturn(true);

        assertTrue(storage.tryAcquire("order-1", 120));

        ArgumentCaptor<Duration> ttl = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).setIfAbsent(eq("test:idempotent:order-1"), eq("1"), ttl.capture());
        assertTrue(ttl.getValue().getSeconds() >= 120);
    }

    @Test
    void tryAcquireReturnsFalseWhenKeyExists() {
        when(valueOperations.setIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);
        assertFalse(storage.tryAcquire("dup", 60));
    }

    @Test
    void tryAcquireRejectsBlankKey() {
        assertFalse(storage.tryAcquire("  ", 60));
    }

    @Test
    void releaseDeletesPrefixedKey() {
        storage.release("order-2");
        verify(redisTemplate).delete("test:idempotent:order-2");
    }
}
