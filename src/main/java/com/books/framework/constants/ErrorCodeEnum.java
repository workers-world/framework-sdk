package com.books.framework.constants;

import lombok.Getter;

/**
 * 错误码枚举类, 统一管理错误码
 *
 * @author zero
 * @description 框架通用错误码定义
 * @date 2024-12-01
 */
@Getter
public enum ErrorCodeEnum {
    // 系统级错误码
    SYSTEM_ERROR("SYSTEM_ERROR", "系统异常"),
    PARAM_ERROR("PARAM_ERROR", "参数错误"),
    PARAM_BIND_ERROR("PARAM_BIND_ERROR", "参数绑定错误"),
    CONSTRAINT_VIOLATION("CONSTRAINT_VIOLATION", "参数校验失败"),
    FILE_NOT_FOUND("FILE_NOT_FOUND", "文件不存在"),
    FILE_READ_ERROR("FILE_READ_ERROR", "文件读取错误"),
    FILE_FORMAT_ERROR("FILE_FORMAT_ERROR", "文件格式错误"),
    DATA_PROCESS_ERROR("DATA_PROCESS_ERROR", "数据处理错误"),

    // 业务级错误码（通用）
    DATA_NOT_FOUND("DATA_001", "数据不存在"),
    DATA_ALREADY_EXISTS("DATA_002", "数据已存在"),
    DATA_INVALID("DATA_003", "数据格式不正确"),
    DATA_EMPTY("DATA_004", "数据不能为空"),
    DATA_STATUS_INVALID("DATA_005", "数据状态不正确"),
    DATA_DELETED("DATA_006", "数据已删除"),
    DATA_BUILD_ERROR("DATA_007", "数据组装出错"),
    DATA_INSERT_ERROR("DATA_008", "数据插入失败"),
    DATA_UPDATE_ERROR("DATA_009", "数据更新失败"),
    DATA_DELETE_ERROR("DATA_010", "数据删除失败"),

    // 用户相关错误码
    USER_NOT_FOUND("USER_001", "用户不存在"),
    USER_ALREADY_EXISTS("USER_002", "用户已存在"),
    USER_PASSWORD_ERROR("USER_003", "密码错误"),
    USER_ACCOUNT_LOCKED("USER_004", "账号已被锁定"),
    USER_ACCOUNT_EXPIRED("USER_005", "账号已过期"),
    USER_CREDENTIALS_EXPIRED("USER_006", "凭证已过期"),
    USER_ACCOUNT_DISABLED("USER_007", "账号已被禁用"),

    // 权限相关错误码
    PERMISSION_DENIED("AUTH_001", "权限不足"),
    TOKEN_INVALID("AUTH_002", "无效的token"),
    TOKEN_EXPIRED("AUTH_003", "token已过期"),
    TOKEN_MISSING("AUTH_004", "token缺失"),

    // 网络相关错误码
    NETWORK_ERROR("NET_001", "网络连接错误"),
    TIMEOUT_ERROR("NET_002", "请求超时"),
    SERVICE_UNAVAILABLE("NET_003", "服务不可用");

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
}
