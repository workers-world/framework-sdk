package com.books.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Books Framework 统一配置（前缀 {@code books.framework}）。
 */
@ConfigurationProperties(prefix = "books.framework")
public class FrameworkProperties {

    private final Trace trace = new Trace();
    private final Idempotent idempotent = new Idempotent();
    private final Progress progress = new Progress();
    private final Redis redis = new Redis();
    private final IdGenerator idGenerator = new IdGenerator();

    public Trace getTrace() {
        return trace;
    }

    public Idempotent getIdempotent() {
        return idempotent;
    }

    public Progress getProgress() {
        return progress;
    }

    public Redis getRedis() {
        return redis;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public static class Trace {
        /** 是否优先使用 Micrometer 当前 Span 的 traceId */
        private boolean micrometerBridgeEnabled = true;

        public boolean isMicrometerBridgeEnabled() {
            return micrometerBridgeEnabled;
        }

        public void setMicrometerBridgeEnabled(boolean micrometerBridgeEnabled) {
            this.micrometerBridgeEnabled = micrometerBridgeEnabled;
        }
    }

    public static class Idempotent {
        /** memory | redis */
        private StoreType storeType = StoreType.memory;

        public StoreType getStoreType() {
            return storeType;
        }

        public void setStoreType(StoreType storeType) {
            this.storeType = storeType;
        }
    }

    public static class Progress {
        private boolean enabled = true;
        /** memory | redis */
        private StoreType storeType = StoreType.memory;
        /** Redis 存储 TTL（秒） */
        private long redisTtlSeconds = 86400;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public StoreType getStoreType() {
            return storeType;
        }

        public void setStoreType(StoreType storeType) {
            this.storeType = storeType;
        }

        public long getRedisTtlSeconds() {
            return redisTtlSeconds;
        }

        public void setRedisTtlSeconds(long redisTtlSeconds) {
            this.redisTtlSeconds = redisTtlSeconds;
        }
    }

    public static class Redis {
        private boolean enabled = false;
        private String keyPrefix = "books:fw:";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getKeyPrefix() {
            return keyPrefix;
        }

        public void setKeyPrefix(String keyPrefix) {
            this.keyPrefix = keyPrefix;
        }
    }

    public static class IdGenerator {
        /** memory | jdbc */
        private IdGeneratorStoreType storeType = IdGeneratorStoreType.jdbc;

        public IdGeneratorStoreType getStoreType() {
            return storeType;
        }

        public void setStoreType(IdGeneratorStoreType storeType) {
            this.storeType = storeType;
        }
    }

    public enum StoreType {
        memory, redis
    }

    public enum IdGeneratorStoreType {
        memory, jdbc
    }
}
