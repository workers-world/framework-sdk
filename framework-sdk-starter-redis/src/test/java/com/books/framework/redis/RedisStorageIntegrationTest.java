package com.books.framework.redis;

import com.books.framework.config.FrameworkProperties;
import com.books.framework.progress.ProgressSnapshot;
import com.books.framework.progress.ProgressStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.testcontainers.RedisContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 真实 Redis 集成测试（需本机 Docker + 本地镜像 {@value #REDIS_IMAGE_NAME}）。
 * <p>默认 {@code mvn test} 会排除 {@code docker} 分组；
 * 显式跑：{@code mvn test -Pdocker-it -pl framework-sdk-starter-redis}。
 * Ryuk 镜像见 {@code .testcontainers.properties}（华为云 SWR）。</p>
 */
@Tag("docker")
@Testcontainers(disabledWithoutDocker = true)
class RedisStorageIntegrationTest {

    /** 与本地已有镜像 tag 完全一致（你本机：redis:7.2-alpine） */
    static final String REDIS_IMAGE_NAME = "redis:7.2-alpine";

    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse(REDIS_IMAGE_NAME);

    @Container
    @SuppressWarnings("resource")
    static final RedisContainer REDIS = new RedisContainer(REDIS_IMAGE)
            .withImagePullPolicy(image -> false);

    private StringRedisTemplate redisTemplate;
    private FrameworkProperties properties;

    @BeforeEach
    void setUp() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(REDIS.getHost(), REDIS.getFirstMappedPort());
        factory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(factory);
        redisTemplate.afterPropertiesSet();

        properties = new FrameworkProperties();
        properties.getRedis().setEnabled(true);
        properties.getRedis().setKeyPrefix("it:");
        properties.getIdempotent().setStoreType(FrameworkProperties.StoreType.redis);
        properties.getProgress().setStoreType(FrameworkProperties.StoreType.redis);
    }

    @Test
    void idempotentStorageRejectsDuplicateKey() {
        RedisIdempotentStorage storage = new RedisIdempotentStorage(redisTemplate, properties);
        assertTrue(storage.tryAcquire("idem-key", 300));
        assertFalse(storage.tryAcquire("idem-key", 300));
        storage.release("idem-key");
        assertTrue(storage.tryAcquire("idem-key", 300));
    }

    @Test
    void progressStoreRoundTrip() {
        RedisProgressStore store = new RedisProgressStore(redisTemplate, new ObjectMapper(), properties);
        ProgressSnapshot snapshot = ProgressSnapshot.builder()
                .taskId("it-task-1")
                .taskType("import")
                .status(ProgressStatus.RUNNING)
                .processed(5)
                .total(100)
                .message("running")
                .updatedAtMs(System.currentTimeMillis())
                .build();

        store.save(snapshot);
        ProgressSnapshot loaded = store.get("it-task-1").orElseThrow();
        assertEquals("import", loaded.getTaskType());
        assertEquals(5, loaded.getProcessed());
        assertEquals(ProgressStatus.RUNNING, loaded.getStatus());
    }
}
