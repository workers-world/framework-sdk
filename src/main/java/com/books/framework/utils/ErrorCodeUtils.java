package com.books.framework.utils;

import com.books.framework.constants.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 错误码管理工具类
 * 
 * @author zero
 * @description 提供错误码的统一管理功能
 * @date 2024-12-01
 */
@Slf4j
public class ErrorCodeUtils {
    
    // 缓存所有注册的错误码枚举
    private static final Map<String, Object> ERROR_CODE_REGISTRY = new ConcurrentHashMap<>();
    
    // 缓存错误码到枚举的映射
    private static final Map<String, Object> CODE_TO_ENUM_MAP = new ConcurrentHashMap<>();
    
    static {
        // 注册框架默认错误码
        registerErrorCodeEnum(ErrorCodeEnum.class);
    }
    
    /**
     * 注册错误码枚举类
     * 
     * @param errorCodeEnumClass 错误码枚举类
     */
    public static void registerErrorCodeEnum(Class<?> errorCodeEnumClass) {
        if (errorCodeEnumClass == null || !errorCodeEnumClass.isEnum()) {
            log.warn("尝试注册非枚举类或空类: {}", errorCodeEnumClass);
            return;
        }
        
        String className = errorCodeEnumClass.getSimpleName();
        ERROR_CODE_REGISTRY.put(className, errorCodeEnumClass);
        
        try {
            // 获取枚举的所有值
            Object[] enumConstants = errorCodeEnumClass.getEnumConstants();
            if (enumConstants != null) {
                for (Object enumConstant : enumConstants) {
                    if (enumConstant instanceof ErrorCodeEnum) {
                        ErrorCodeEnum errorCode = (ErrorCodeEnum) enumConstant;
                        CODE_TO_ENUM_MAP.put(errorCode.getCode(), errorCode);
                    } else {
                        // 处理其他类型的错误码枚举
                        try {
                            String code = (String) enumConstant.getClass().getMethod("getCode").invoke(enumConstant);
                            CODE_TO_ENUM_MAP.put(code, enumConstant);
                        } catch (Exception e) {
                            log.warn("无法获取错误码枚举的code值: {}", enumConstant);
                        }
                    }
                }
            }
            log.info("成功注册错误码枚举: {}", className);
        } catch (Exception e) {
            log.error("注册错误码枚举失败: {}", className, e);
        }
    }
    
    /**
     * 根据错误码获取错误码枚举
     * 
     * @param code 错误码
     * @return 错误码枚举
     */
    public static Object getErrorCodeEnum(String code) {
        return CODE_TO_ENUM_MAP.get(code);
    }
    
    /**
     * 检查错误码是否存在
     * 
     * @param code 错误码
     * @return 是否存在
     */
    public static boolean exists(String code) {
        return CODE_TO_ENUM_MAP.containsKey(code);
    }
    
    /**
     * 获取所有注册的错误码枚举类
     * 
     * @return 错误码枚举类映射
     */
    public static Map<String, Object> getRegisteredErrorCodeEnums() {
        return new HashMap<>(ERROR_CODE_REGISTRY);
    }
    
    /**
     * 获取所有错误码
     * 
     * @return 错误码集合
     */
    public static Map<String, Object> getAllErrorCodes() {
        return new HashMap<>(CODE_TO_ENUM_MAP);
    }
    
    /**
     * 清除所有注册的错误码
     */
    public static void clear() {
        ERROR_CODE_REGISTRY.clear();
        CODE_TO_ENUM_MAP.clear();
        // 重新注册框架默认错误码
        registerErrorCodeEnum(ErrorCodeEnum.class);
    }
    
    /**
     * 获取错误码统计信息
     * 
     * @return 统计信息
     */
    public static String getStatistics() {
        return String.format("已注册错误码枚举类: %d, 总错误码数量: %d", 
                ERROR_CODE_REGISTRY.size(), CODE_TO_ENUM_MAP.size());
    }
    
    /**
     * 验证错误码格式
     * 
     * @param code 错误码
     * @return 是否格式正确
     */
    public static boolean isValidFormat(String code) {
        if (StringUtils.isEmpty(code)) {
            return false;
        }
        
        // 错误码格式规则：
        // 1. 不能为空
        // 2. 不能包含特殊字符（除了下划线和数字）
        // 3. 长度在3-50之间
        return code.matches("^[A-Z][A-Z0-9_]{2,49}$");
    }
    
    /**
     * 生成错误码建议
     * 
     * @param prefix 前缀
     * @param description 描述
     * @return 建议的错误码
     */
    public static String suggestErrorCode(String prefix, String description) {
        if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(description)) {
            return "UNKNOWN_ERROR";
        }
        
        // 将描述转换为大写，替换空格为下划线
        String suggestion = prefix.toUpperCase() + "_" + 
                description.toUpperCase().replaceAll("\\s+", "_");
        
        // 移除特殊字符
        suggestion = suggestion.replaceAll("[^A-Z0-9_]", "");
        
        // 确保长度合适
        if (suggestion.length() > 50) {
            suggestion = suggestion.substring(0, 50);
        }
        
        return suggestion;
    }
}
