package com.books.framework.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

import java.util.ArrayList;
import java.util.List;

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
    private final AccessLog accessLog = new AccessLog();

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

    public AccessLog getAccessLog() {
        return accessLog;
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

    public static class AccessLog {
        private boolean enabled = true;
        private boolean requireApiName = false;
        private boolean logRequestBody = false;
        private boolean logResponseBody = false;
        private int maxBodyLength = 2000;
        private List<String> maskFields = new ArrayList<>(List.of("sign", "password", "token", "secret"));
        private List<String> excludePathPatterns = new ArrayList<>(List.of(
                "/actuator/**",
                "/api/progress/**",
                "/uploads/**"
        ));
        private boolean outboundEnabled = true;
        private boolean outboundLogRequestBody = false;
        private boolean outboundLogResponseBody = false;
        private List<String> outboundExcludeUrlPatterns = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isRequireApiName() {
            return requireApiName;
        }

        public void setRequireApiName(boolean requireApiName) {
            this.requireApiName = requireApiName;
        }

        public boolean isLogRequestBody() {
            return logRequestBody;
        }

        public void setLogRequestBody(boolean logRequestBody) {
            this.logRequestBody = logRequestBody;
        }

        public boolean isLogResponseBody() {
            return logResponseBody;
        }

        public void setLogResponseBody(boolean logResponseBody) {
            this.logResponseBody = logResponseBody;
        }

        public int getMaxBodyLength() {
            return maxBodyLength;
        }

        public void setMaxBodyLength(int maxBodyLength) {
            this.maxBodyLength = maxBodyLength;
        }

        public List<String> getMaskFields() {
            return maskFields;
        }

        public void setMaskFields(List<String> maskFields) {
            this.maskFields = maskFields;
        }

        public List<String> getExcludePathPatterns() {
            return excludePathPatterns;
        }

        public void setExcludePathPatterns(List<String> excludePathPatterns) {
            this.excludePathPatterns = excludePathPatterns;
        }

        public boolean isOutboundEnabled() {
            return outboundEnabled;
        }

        public void setOutboundEnabled(boolean outboundEnabled) {
            this.outboundEnabled = outboundEnabled;
        }

        public boolean isOutboundLogRequestBody() {
            return outboundLogRequestBody;
        }

        public void setOutboundLogRequestBody(boolean outboundLogRequestBody) {
            this.outboundLogRequestBody = outboundLogRequestBody;
        }

        public boolean isOutboundLogResponseBody() {
            return outboundLogResponseBody;
        }

        public void setOutboundLogResponseBody(boolean outboundLogResponseBody) {
            this.outboundLogResponseBody = outboundLogResponseBody;
        }

        public List<String> getOutboundExcludeUrlPatterns() {
            return outboundExcludeUrlPatterns;
        }

        public void setOutboundExcludeUrlPatterns(List<String> outboundExcludeUrlPatterns) {
            this.outboundExcludeUrlPatterns = outboundExcludeUrlPatterns;
        }
    }
}
