package com.books.framework.message;

import lombok.Data;

/**
 * 统一响应结果类
 *
 * @author zero
 * @description 框架通用统一响应结果类
 * @date 2024-12-01
 */
@Data
public class ResponseResult<T> {
    private String code;
    private String message;
    private T data;

    private ResponseResult(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 创建成功响应（无数据）
     *
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success() {
        return new ResponseResult<>("SUCCESS", "操作成功", null);
    }

    /**
     * 创建成功响应（有数据）
     *
     * @param data 响应数据
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success(T data) {
        return new ResponseResult<>("SUCCESS", "操作成功", data);
    }

    /**
     * 创建成功响应（自定义消息）
     *
     * @param message 响应消息
     * @param data    响应数据
     * @return 响应结果
     */
    public static <T> ResponseResult<T> success(String message, T data) {
        return new ResponseResult<>("SUCCESS", message, data);
    }

    /**
     * 创建错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 响应结果
     */
    public static <T> ResponseResult<T> error(String code, String message) {
        return new ResponseResult<>(code, message, null);
    }

    /**
     * 创建错误响应（有数据）
     *
     * @param code    错误码
     * @param message 错误消息
     * @param data    错误数据
     * @return 响应结果
     */
    public static <T> ResponseResult<T> error(String code, String message, T data) {
        return new ResponseResult<>(code, message, data);
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        return "SUCCESS".equals(this.code);
    }

    /**
     * 判断是否失败
     *
     * @return 是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}
