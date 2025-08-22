package com.books.framework.message;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ResponseResult测试类
 *
 * @author zero
 * @description ResponseResult统一响应结果类测试
 * @date 2024-12-01
 */
class ResponseResultTest {

    @Test
    void testSuccessWithoutData() {
        ResponseResult<String> result = ResponseResult.success();

        assertEquals("SUCCESS", result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertNull(result.getData());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testSuccessWithData() {
        String data = "test data";
        ResponseResult<String> result = ResponseResult.success(data);

        assertEquals("SUCCESS", result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals(data, result.getData());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testSuccessWithCustomMessage() {
        String data = "test data";
        String message = "自定义成功消息";
        ResponseResult<String> result = ResponseResult.success(message, data);

        assertEquals("SUCCESS", result.getCode());
        assertEquals(message, result.getMessage());
        assertEquals(data, result.getData());
        assertTrue(result.isSuccess());
        assertFalse(result.isError());
    }

    @Test
    void testError() {
        String errorCode = "ERROR_001";
        String errorMessage = "错误信息";
        ResponseResult<String> result = ResponseResult.error(errorCode, errorMessage);

        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testErrorWithData() {
        String errorCode = "ERROR_001";
        String errorMessage = "错误信息";
        String errorData = "错误数据";
        ResponseResult<String> result = ResponseResult.error(errorCode, errorMessage, errorData);

        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertEquals(errorData, result.getData());
        assertFalse(result.isSuccess());
        assertTrue(result.isError());
    }

    @Test
    void testIsSuccessAndIsError() {
        ResponseResult<String> successResult = ResponseResult.success();
        ResponseResult<String> errorResult = ResponseResult.error("ERROR", "错误");

        assertTrue(successResult.isSuccess());
        assertFalse(successResult.isError());

        assertFalse(errorResult.isSuccess());
        assertTrue(errorResult.isError());
    }
}
