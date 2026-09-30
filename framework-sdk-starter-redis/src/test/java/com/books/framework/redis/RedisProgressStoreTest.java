package com.books.framework.redis;

import com.books.framework.config.FrameworkProperties;
import com.books.framework.progress.ProgressSnapshot;
import com.books.framework.progress.ProgressStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisProgressStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisProgressStore store;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        FrameworkProperties properties = new FrameworkProperties();
        properties.getRedis().setKeyPrefix("test:");
        properties.getProgress().setRedisTtlSeconds(3600);
        store = new RedisProgressStore(redisTemplate, objectMapper, properties);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void saveWritesJsonWithTtl() throws Exception {
        ProgressSnapshot snapshot = ProgressSnapshot.builder()
                .taskId("task-1")
                .status(ProgressStatus.RUNNING)
                .processed(1)
                .total(10)
                .build();

        store.save(snapshot);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq("test:progress:task-1"), jsonCaptor.capture(), any(Duration.class));
        ProgressSnapshot parsed = objectMapper.readValue(jsonCaptor.getValue(), ProgressSnapshot.class);
        assertEquals("task-1", parsed.getTaskId());
        assertEquals(ProgressStatus.RUNNING, parsed.getStatus());
    }

    @Test
    void getReturnsEmptyWhenMissing() {
        when(valueOperations.get("test:progress:missing")).thenReturn(null);
        assertTrue(store.get("missing").isEmpty());
    }

    @Test
    void getDeserializesSnapshot() throws Exception {
        ProgressSnapshot original = ProgressSnapshot.builder()
                .taskId("task-2")
                .status(ProgressStatus.COMPLETED)
                .message("done")
                .build();
        String json = objectMapper.writeValueAsString(original);
        when(valueOperations.get("test:progress:task-2")).thenReturn(json);

        Optional<ProgressSnapshot> loaded = store.get("task-2");
        assertTrue(loaded.isPresent());
        assertEquals(ProgressStatus.COMPLETED, loaded.get().getStatus());
        assertEquals("done", loaded.get().getMessage());
    }
}
