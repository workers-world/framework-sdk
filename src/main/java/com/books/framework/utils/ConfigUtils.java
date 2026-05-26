package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置工具类——统一委托给 Spring {@link Environment}（通过 {@link #setEnvironment(Environment)} 注入）。
 * <p>未注入 Environment 时回退到内存缓存（Set/Get），方便非 Spring 上下文或单测。</p>
 */
@Slf4j
public final class ConfigUtils {

    private static volatile Environment environment;
    private static final ConcurrentHashMap<String, Object> CACHE = new ConcurrentHashMap<>();

    private ConfigUtils() {
    }

    /**
     * 由自动装配调用，注入 Spring Environment。
     */
    public static void setEnvironment(Environment env) {
        environment = env;
    }

    // ---- 字符串 ----

    public static String getString(String key) {
        if (!StringUtils.isNotBlank(key)) {
            return null;
        }
        Environment env = environment;
        if (env != null) {
            String v = env.getProperty(key);
            if (v != null) {
                return v;
            }
        }
        Object cached = CACHE.get(key);
        return cached != null ? cached.toString() : null;
    }

    public static String getString(String key, String defaultValue) {
        String v = getString(key);
        return v != null ? v : defaultValue;
    }

    // ---- 整数 ----

    public static Integer getInteger(String key, Integer defaultValue) {
        Environment env = environment;
        if (env != null) {
            Integer v = env.getProperty(key, Integer.class);
            if (v != null) {
                return v;
            }
        }
        String s = getString(key);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            log.warn("配置 {} 的值 {} 不是有效的整数", key, s);
            return defaultValue;
        }
    }

    public static Long getLong(String key, Long defaultValue) {
        Environment env = environment;
        if (env != null) {
            Long v = env.getProperty(key, Long.class);
            if (v != null) {
                return v;
            }
        }
        String s = getString(key);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            log.warn("配置 {} 的值 {} 不是有效的长整数", key, s);
            return defaultValue;
        }
    }

    public static Boolean getBoolean(String key, Boolean defaultValue) {
        Environment env = environment;
        if (env != null) {
            Boolean v = env.getProperty(key, Boolean.class);
            if (v != null) {
                return v;
            }
        }
        String s = getString(key);
        if (s == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(s.trim());
    }

    public static Double getDouble(String key, Double defaultValue) {
        Environment env = environment;
        if (env != null) {
            Double v = env.getProperty(key, Double.class);
            if (v != null) {
                return v;
            }
        }
        String s = getString(key);
        if (s == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            log.warn("配置 {} 的值 {} 不是有效的浮点数", key, s);
            return defaultValue;
        }
    }

    // ---- 缓存操作 ----

    public static void setConfig(String key, Object value) {
        if (StringUtils.isNotBlank(key)) {
            CACHE.put(key, value);
        }
    }

    public static void removeConfig(String key) {
        if (StringUtils.isNotBlank(key)) {
            CACHE.remove(key);
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }

    public static boolean hasConfig(String key) {
        return getString(key) != null;
    }

    public static Set<String> getAllKeys() {
        return CACHE.keySet();
    }

    public static String getStatistics() {
        return "配置缓存大小: " + CACHE.size();
    }
}
