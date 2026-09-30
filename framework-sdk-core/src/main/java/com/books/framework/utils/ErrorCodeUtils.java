package com.books.framework.utils;

import com.books.framework.constants.ErrorCode;
import com.books.framework.constants.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ErrorCodeUtils {

    private static final Map<String, Object> ERROR_CODE_REGISTRY = new ConcurrentHashMap<>();

    private static final Map<String, Object> CODE_TO_ENUM_MAP = new ConcurrentHashMap<>();

    static {
        registerErrorCodeEnum(ErrorCodeEnum.class);
    }

    public static void registerErrorCodeEnum(Class<?> errorCodeEnumClass) {
        if (errorCodeEnumClass == null || !errorCodeEnumClass.isEnum()) {
            log.warn("尝试注册非枚举类或空类: {}", errorCodeEnumClass);
            return;
        }

        String className = errorCodeEnumClass.getSimpleName();
        ERROR_CODE_REGISTRY.put(className, errorCodeEnumClass);

        try {
            Object[] enumConstants = errorCodeEnumClass.getEnumConstants();
            if (enumConstants != null) {
                for (Object enumConstant : enumConstants) {
                    registerCodeConstant(enumConstant, errorCodeEnumClass);
                }
            }
            log.info("成功注册错误码枚举: {}", className);
        } catch (Exception e) {
            log.error("注册错误码枚举失败: {}", className, e);
            throw new IllegalStateException("注册错误码枚举失败: " + className, e);
        }
    }

    private static void registerCodeConstant(Object enumConstant, Class<?> sourceClass) {
        String code = resolveCode(enumConstant);
        if (StringUtils.isBlank(code)) {
            log.warn("无法获取错误码枚举的 code 值: {}", enumConstant);
            return;
        }
        Object existing = CODE_TO_ENUM_MAP.putIfAbsent(code, enumConstant);
        if (existing != null && existing != enumConstant) {
            throw new IllegalStateException(String.format(
                    "错误码 %s 冲突：已注册于 %s，无法再次注册于 %s",
                    code, existing.getClass().getName(), sourceClass.getName()));
        }
    }

    private static String resolveCode(Object enumConstant) {
        if (enumConstant instanceof ErrorCode errorCode) {
            return errorCode.getCode();
        }
        try {
            Method getCode = enumConstant.getClass().getMethod("getCode");
            Object value = getCode.invoke(enumConstant);
            return value instanceof String s ? s : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Object getErrorCodeEnum(String code) {
        return CODE_TO_ENUM_MAP.get(code);
    }

    public static boolean exists(String code) {
        return CODE_TO_ENUM_MAP.containsKey(code);
    }

    public static Map<String, Object> getRegisteredErrorCodeEnums() {
        return new HashMap<>(ERROR_CODE_REGISTRY);
    }

    public static Map<String, Object> getAllErrorCodes() {
        return new HashMap<>(CODE_TO_ENUM_MAP);
    }

    public static void clear() {
        ERROR_CODE_REGISTRY.clear();
        CODE_TO_ENUM_MAP.clear();
        registerErrorCodeEnum(ErrorCodeEnum.class);
    }

    public static String getStatistics() {
        return String.format("已注册错误码枚举类: %d, 总错误码数量: %d",
                ERROR_CODE_REGISTRY.size(), CODE_TO_ENUM_MAP.size());
    }

    public static boolean isValidFormat(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        return code.matches("^[A-Z0-9_]{3,50}$");
    }

    public static String suggestErrorCode(String prefix, String description) {
        if (StringUtils.isBlank(prefix) || StringUtils.isBlank(description)) {
            return "UNKNOWN_ERROR";
        }

        String suggestion = prefix.toUpperCase() + "_"
                + description.toUpperCase().replaceAll("\\s+", "_");
        suggestion = suggestion.replaceAll("[^A-Z0-9_]", "");
        if (suggestion.length() > 50) {
            suggestion = suggestion.substring(0, 50);
        }
        return suggestion;
    }
}
