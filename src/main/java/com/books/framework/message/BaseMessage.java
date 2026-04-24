package com.books.framework.message;

import com.alibaba.fastjson.JSON;
import com.books.framework.utils.DateUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用消息基类
 *
 * @author zero
 * @description 框架通用消息基类，用于统一消息格式
 * @date 2024-12-01
 */
@Data
public class BaseMessage {
    /**
     * 消息ID，用于追踪
     */
    private String messageId;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 来源
     */
    private String source;

    /**
     * 版本
     */
    private String version;

    /**
     * 签名
     */
    private String sign;

    /**
     * 响应码（仅响应时使用）
     */
    private String code;

    /**
     * 响应消息（仅响应时使用）
     */
    private String message;

    /**
     * 响应时间（仅响应时使用）
     */
    private String responseTime;

    /**
     * 业务数据集合
     * key: 业务数据类型
     * value: 业务数据对象
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 添加单个对象
     *
     * @param dto 业务数据对象
     */
    public void addDto(Object dto) {
        if (dto == null) {
            return;
        }
        String type = dto.getClass().getSimpleName();
        this.data.put(type, dto);
    }

    /**
     * 添加对象列表
     *
     * @param dtoList 业务数据列表
     */
    public void addDtoList(List<?> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        Object firstElement = dtoList.get(0);
        if (firstElement != null) {
            String type = firstElement.getClass().getSimpleName();
            this.data.put(type, dtoList);
        }
    }

    /**
     * 获取单个对象
     *
     * @param clazz 目标类型
     * @return 转换后的业务数据对象
     */
    public <T> T getDto(Class<T> clazz) {
        String type = clazz.getSimpleName();
        Object data = this.data.get(type);
        if (data == null) {
            return null;
        }

        // 如果已经是目标类型，直接返回
        if (clazz.isInstance(data)) {
            return clazz.cast(data);
        }

        // 如果是字符串，尝试反序列化
        if (data instanceof String) {
            return JSON.parseObject((String) data, clazz);
        }

        // 如果是其他类型，先转为JSON字符串再反序列化
        return JSON.parseObject(JSON.toJSONString(data), clazz);
    }

    /**
     * 获取对象列表
     *
     * @param clazz 列表元素类型
     * @return 转换后的业务数据列表
     */
    public <T> List<T> getDtoList(Class<T> clazz) {
        String type = clazz.getSimpleName();
        Object data = this.data.get(type);
        if (data == null) {
            return null;
        }

        // 如果已经是List类型，直接返回
        if (data instanceof List) {
            return (List<T>) data;
        }

        // 如果是字符串，尝试反序列化为列表
        if (data instanceof String) {
            return JSON.parseArray((String) data, clazz);
        }

        // 如果是其他类型，先转为JSON字符串再反序列化为列表
        return JSON.parseArray(JSON.toJSONString(data), clazz);
    }

    /**
     * 创建成功响应
     *
     * @param request 请求消息
     * @return 响应消息
     */
    public static BaseMessage newResponse(BaseMessage request) {
        BaseMessage response = copyRequestMeta(request);
        response.setCode("SUCCESS");
        response.setMessage("操作成功");
        response.setResponseTime(DateUtils.getCurDateTime());
        return response;
    }

    /**
     * 基于当前请求对象创建成功响应（实例写法）。
     *
     * @return 响应消息
     */
    public BaseMessage newResponse() {
        return newResponse(this);
    }

    /**
     * 创建错误响应
     *
     * @param request 请求消息
     * @param code    错误码
     * @param message 错误消息
     * @return 响应消息
     */
    public static BaseMessage error(BaseMessage request, String code, String message) {
        BaseMessage response = copyRequestMeta(request);
        response.setCode(code);
        response.setMessage(message);
        response.setResponseTime(DateUtils.getCurDateTime());
        return response;
    }

    /**
     * 基于当前请求对象创建失败响应（实例写法）。
     *
     * @param code    错误码
     * @param message 错误消息
     * @return 响应消息
     */
    public BaseMessage newError(String code, String message) {
        return error(this, code, message);
    }

    /**
     * 创建新的请求消息
     *
     * @param messageId 消息ID
     * @param source    来源
     * @param version   版本
     * @return 请求消息
     */
    public static BaseMessage newRequest(String messageId, String source, String version) {
        BaseMessage request = new BaseMessage();
        request.setMessageId(messageId);
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.setSource(source);
        request.setVersion(version);
        return request;
    }

    private static BaseMessage copyRequestMeta(BaseMessage request) {
        BaseMessage response = new BaseMessage();
        if (request == null) {
            return response;
        }
        response.setMessageId(request.getMessageId());
        response.setTimestamp(request.getTimestamp());
        response.setSource(request.getSource());
        response.setVersion(request.getVersion());
        response.setSign(request.getSign());
        return response;
    }
}
