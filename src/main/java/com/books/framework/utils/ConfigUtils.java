package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 配置管理工具类
 * 
 * @author zero
 * @description 提供配置文件读取、环境变量管理、配置验证等功能
 * @date 2024-12-01
 */
@Slf4j
public class ConfigUtils {
    
    // 配置缓存
    private static final ConcurrentHashMap<String, Object> CONFIG_CACHE = new ConcurrentHashMap<>();
    
    // 默认配置文件路径
    private static final String DEFAULT_CONFIG_FILE = "application.properties";
    
    // 环境变量前缀
    private static final String ENV_PREFIX = "APP_";
    
    /**
     * 从配置文件读取字符串配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static String getString(String key, String defaultValue) {
        String value = getString(key);
        return StringUtils.hasText(value) ? value : defaultValue;
    }
    
    /**
     * 从配置文件读取字符串配置
     * 
     * @param key 配置键
     * @return 配置值
     */
    public static String getString(String key) {
        if (!StringUtils.hasText(key)) {
            return null;
        }
        
        // 先从缓存获取
        Object cached = CONFIG_CACHE.get(key);
        if (cached != null) {
            return cached.toString();
        }
        
        // 从环境变量获取
        String envValue = getEnvValue(key);
        if (StringUtils.hasText(envValue)) {
            CONFIG_CACHE.put(key, envValue);
            return envValue;
        }
        
        // 从配置文件获取
        String configValue = getConfigValue(key);
        if (StringUtils.hasText(configValue)) {
            CONFIG_CACHE.put(key, configValue);
            return configValue;
        }
        
        return null;
    }
    
    /**
     * 从配置文件读取整数配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static Integer getInteger(String key, Integer defaultValue) {
        String value = getString(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 不是有效的整数，使用默认值 {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 从配置文件读取长整数配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static Long getLong(String key, Long defaultValue) {
        String value = getString(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 不是有效的长整数，使用默认值 {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 从配置文件读取布尔配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static Boolean getBoolean(String key, Boolean defaultValue) {
        String value = getString(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        
        return Boolean.parseBoolean(value.trim());
    }
    
    /**
     * 从配置文件读取双精度浮点数配置
     * 
     * @param key 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    public static Double getDouble(String key, Double defaultValue) {
        String value = getString(key);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            log.warn("配置项 {} 的值 {} 不是有效的浮点数，使用默认值 {}", key, value, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 设置配置值
     * 
     * @param key 配置键
     * @param value 配置值
     */
    public static void setConfig(String key, Object value) {
        if (StringUtils.hasText(key)) {
            CONFIG_CACHE.put(key, value);
        }
    }
    
    /**
     * 删除配置
     * 
     * @param key 配置键
     */
    public static void removeConfig(String key) {
        if (StringUtils.hasText(key)) {
            CONFIG_CACHE.remove(key);
        }
    }
    
    /**
     * 清空配置缓存
     */
    public static void clearCache() {
        CONFIG_CACHE.clear();
    }
    
    /**
     * 检查配置是否存在
     * 
     * @param key 配置键
     * @return 是否存在
     */
    public static boolean hasConfig(String key) {
        return StringUtils.hasText(getString(key));
    }
    
    /**
     * 获取所有配置键
     * 
     * @return 配置键集合
     */
    public static java.util.Set<String> getAllKeys() {
        return CONFIG_CACHE.keySet();
    }
    
    /**
     * 获取配置统计信息
     * 
     * @return 统计信息
     */
    public static String getStatistics() {
        return String.format("配置缓存大小: %d", CONFIG_CACHE.size());
    }
    
    /**
     * 从环境变量获取值
     * 
     * @param key 配置键
     * @return 环境变量值
     */
    private static String getEnvValue(String key) {
        // 尝试直接获取
        String value = System.getenv(key);
        if (StringUtils.hasText(value)) {
            return value;
        }
        
        // 尝试带前缀获取
        value = System.getenv(ENV_PREFIX + key);
        if (StringUtils.hasText(value)) {
            return value;
        }
        
        // 尝试转换为大写
        value = System.getenv(key.toUpperCase());
        if (StringUtils.hasText(value)) {
            return value;
        }
        
        return null;
    }
    
    /**
     * 从配置文件获取值
     * 
     * @param key 配置键
     * @return 配置值
     */
    private static String getConfigValue(String key) {
        try {
            Properties props = loadProperties();
            return props.getProperty(key);
        } catch (Exception e) {
            log.debug("无法从配置文件读取配置项: {}", key, e);
            return null;
        }
    }
    
    /**
     * 加载配置文件
     * 
     * @return Properties对象
     * @throws IOException IO异常
     */
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        
        // 尝试加载默认配置文件
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (input != null) {
                props.load(input);
                log.debug("成功加载配置文件: {}", DEFAULT_CONFIG_FILE);
            }
        }
        
        // 尝试加载yml配置文件
        try (InputStream input = ConfigUtils.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (input != null) {
                // 简单的yml解析（实际项目中建议使用SnakeYAML）
                log.debug("检测到YAML配置文件，建议使用Spring Boot的配置管理");
            }
        }
        
        return props;
    }
    
    /**
     * 验证必需配置项
     * 
     * @param keys 必需配置键数组
     * @throws IllegalArgumentException 如果缺少必需配置项
     */
    public static void validateRequiredConfigs(String... keys) {
        if (keys == null || keys.length == 0) {
            return;
        }
        
        StringBuilder missingKeys = new StringBuilder();
        for (String key : keys) {
            if (!hasConfig(key)) {
                if (missingKeys.length() > 0) {
                    missingKeys.append(", ");
                }
                missingKeys.append(key);
            }
        }
        
        if (missingKeys.length() > 0) {
            throw new IllegalArgumentException("缺少必需的配置项: " + missingKeys.toString());
        }
    }
    
    /**
     * 获取配置前缀的所有配置
     * 
     * @param prefix 配置前缀
     * @return 配置映射
     */
    public static java.util.Map<String, String> getConfigsByPrefix(String prefix) {
        java.util.Map<String, String> result = new java.util.HashMap<>();
        
        if (!StringUtils.hasText(prefix)) {
            return result;
        }
        
        try {
            Properties props = loadProperties();
            for (String key : props.stringPropertyNames()) {
                if (key.startsWith(prefix)) {
                    result.put(key, props.getProperty(key));
                }
            }
        } catch (Exception e) {
            log.warn("获取前缀配置失败: {}", prefix, e);
        }
        
        return result;
    }
}
