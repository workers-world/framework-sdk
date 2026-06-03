package com.books.framework.exception;

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

    public AppException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = errorCodeEnum.getMessage();
    }

    public AppException(ErrorCodeEnum errorCodeEnum, String message) {
        super(message);
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = message;
    }

    public AppException(ErrorCodeEnum errorCodeEnum, Throwable cause) {
        super(errorCodeEnum.getMessage(), cause);
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = errorCodeEnum.getMessage();
    }

    public AppException(ErrorCodeEnum errorCodeEnum, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCodeEnum.getCode();
        this.errorMessage = message;
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
