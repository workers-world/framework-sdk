package com.books.framework.constants;

import lombok.Getter;

/** 框架通用错误码枚举。 */
@Getter
public enum ErrorCodeEnum implements ErrorCode {
    // 系统级错误码
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常"),
    PARAM_ERROR("PARAM_ERROR", "参数错误"),
    PARAM_BIND_ERROR("PARAM_BIND_ERROR", "参数绑定错误"),
    CONSTRAINT_VIOLATION("CONSTRAINT_VIOLATION", "参数校验失败"),
    FILE_NOT_FOUND("FILE_NOT_FOUND", "文件不存在"),
    FILE_READ_ERROR("FILE_READ_ERROR", "文件读取错误"),
    FILE_FORMAT_ERROR("FILE_FORMAT_ERROR", "文件格式错误"),
    DATA_PROCESS_ERROR("DATA_PROCESS_ERROR", "数据处理错误"),
    CONFIG_ERROR("CONFIG_ERROR", "配置错误"),
    DATABASE_ERROR("DATABASE_ERROR", "数据库错误"),
    CACHE_ERROR("CACHE_ERROR", "缓存错误"),
    ENCRYPTION_ERROR("ENCRYPTION_ERROR", "加密解密错误"),
    SERIALIZATION_ERROR("SERIALIZATION_ERROR", "序列化错误"),
    DESERIALIZATION_ERROR("DESERIALIZATION_ERROR", "反序列化错误"),

    // 通用业务错误码
    DATA_NOT_FOUND("DATA_NOT_FOUND", "数据不存在"),
    DATA_ALREADY_EXISTS("DATA_ALREADY_EXISTS", "数据已存在"),
    DATA_INVALID("DATA_INVALID", "数据格式不正确"),
    DATA_EMPTY("DATA_EMPTY", "数据不能为空"),
    DATA_STATUS_INVALID("DATA_STATUS_INVALID", "数据状态不正确"),
    DATA_DELETED("DATA_DELETED", "数据已删除"),
    DATA_BUILD_ERROR("DATA_BUILD_ERROR", "数据组装出错"),
    DATA_INSERT_ERROR("DATA_INSERT_ERROR", "数据插入失败"),
    DATA_UPDATE_ERROR("DATA_UPDATE_ERROR", "数据更新失败"),
    DATA_DELETE_ERROR("DATA_DELETE_ERROR", "数据删除失败"),
    DATA_QUERY_ERROR("DATA_QUERY_ERROR", "数据查询失败"),
    DATA_VALIDATION_ERROR("DATA_VALIDATION_ERROR", "数据验证失败"),

    // 权限相关错误码
    PERMISSION_DENIED("PERMISSION_DENIED", "权限不足"),
    TOKEN_INVALID("TOKEN_INVALID", "无效的token"),
    TOKEN_EXPIRED("TOKEN_EXPIRED", "token已过期"),
    TOKEN_MISSING("TOKEN_MISSING", "token缺失"),
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", "认证失败"),
    AUTHORIZATION_FAILED("AUTHORIZATION_FAILED", "授权失败"),

    // 网络相关错误码
    NETWORK_ERROR("NETWORK_ERROR", "网络连接错误"),
    TIMEOUT_ERROR("TIMEOUT_ERROR", "请求超时"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE", "服务不可用"),
    REMOTE_SERVICE_ERROR("REMOTE_SERVICE_ERROR", "远程服务错误"),
    API_CALL_ERROR("API_CALL_ERROR", "API调用错误"),

    // 资源相关错误码
    RESOURCE_NOT_FOUND("RESOURCE_NOT_FOUND", "资源不存在"),
    RESOURCE_ACCESS_DENIED("RESOURCE_ACCESS_DENIED", "资源访问被拒绝"),
    RESOURCE_LOCKED("RESOURCE_LOCKED", "资源被锁定"),
    RESOURCE_EXHAUSTED("RESOURCE_EXHAUSTED", "资源耗尽"),

    // 操作相关错误码
    OPERATION_FAILED("OPERATION_FAILED", "操作失败"),
    OPERATION_NOT_SUPPORTED("OPERATION_NOT_SUPPORTED", "操作不支持"),
    OPERATION_TIMEOUT("OPERATION_TIMEOUT", "操作超时"),
    OPERATION_CANCELLED("OPERATION_CANCELLED", "操作已取消"),
    OPERATION_IN_PROGRESS("OPERATION_IN_PROGRESS", "操作进行中"),

    // 格式相关错误码
    FORMAT_ERROR("FORMAT_ERROR", "格式错误"),
    ENCODING_ERROR("ENCODING_ERROR", "编码错误"),
    DECODING_ERROR("DECODING_ERROR", "解码错误"),
    PARSING_ERROR("PARSING_ERROR", "解析错误"),

    // 状态相关错误码
    STATE_ERROR("STATE_ERROR", "状态错误"),
    STATE_TRANSITION_ERROR("STATE_TRANSITION_ERROR", "状态转换错误"),
    INVALID_STATE("INVALID_STATE", "无效状态"),

    // 版本相关错误码
    VERSION_MISMATCH("VERSION_MISMATCH", "版本不匹配"),
    VERSION_NOT_SUPPORTED("VERSION_NOT_SUPPORTED", "版本不支持"),
    DEPRECATED_FEATURE("DEPRECATED_FEATURE", "功能已废弃");

    private final String code;
    private final String message;

    ErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误码获取错误码枚举
     *
     * @param code 错误码
     * @return 错误码枚举
     */
    public static ErrorCodeEnum getByCode(String code) {
        for (ErrorCodeEnum errorCode : values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 检查错误码是否存在
     *
     * @param code 错误码
     * @return 是否存在
     */
    public static boolean exists(String code) {
        return getByCode(code) != null;
    }
}
