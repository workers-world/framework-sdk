package com.books.framework.exception;

import com.books.framework.constants.ErrorCode;
import com.books.framework.constants.ErrorCodeEnum;
import lombok.Getter;

/**
 * 自定义应用异常类
 *
 * @author zero
 * @description 框架通用异常类
 * @date 2024-12-01
 */
@Getter
public class AppException extends RuntimeException {
    // 定义一个序列化ID (serialVersionUID)，这有助于保证异常对象在序列化和反序列化过程中的兼容性。
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final String errorMessage;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getMessage();
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode.getCode();
        this.errorMessage = message;
    }

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode.getCode();
        this.errorMessage = errorCode.getMessage();
    }

    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode.getCode();
        this.errorMessage = message;
    }

    public AppException(ErrorCodeEnum errorCodeEnum) {
        this((ErrorCode) errorCodeEnum);
    }

    public AppException(ErrorCodeEnum errorCodeEnum, String message) {
        this((ErrorCode) errorCodeEnum, message);
    }

    public AppException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        this((ErrorCode) errorCodeEnum, cause);
    }

    public AppException(ErrorCodeEnum errorCodeEnum, String message, Throwable cause) {
        this((ErrorCode) errorCodeEnum, message, cause);
    }

    public AppException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public AppException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return String.format("错误码：%s，错误信息：%s", errorCode, errorMessage);
    }
}
