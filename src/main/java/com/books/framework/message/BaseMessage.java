package com.books.framework.message;

import com.books.framework.utils.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static volatile ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 设置全局 ObjectMapper（通常由 Spring 配置注入）。
     */
    public static void setObjectMapper(ObjectMapper mapper) {
        objectMapper = mapper != null ? mapper : new ObjectMapper();
    }

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
     */
    private Map<String, Object> data = new HashMap<>();

    // ==================== 链式 Builder ====================

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final BaseMessage msg = new BaseMessage();

        private Builder() {
        }

        public Builder messageId(String messageId) {
            msg.messageId = messageId;
            return this;
        }

        public Builder timestamp(String timestamp) {
            msg.timestamp = timestamp;
            return this;
        }

        public Builder source(String source) {
            msg.source = source;
            return this;
        }

        public Builder version(String version) {
            msg.version = version;
            return this;
        }

        public Builder sign(String sign) {
            msg.sign = sign;
            return this;
        }

        public Builder code(String code) {
            msg.code = code;
            return this;
        }

        public Builder message(String message) {
            msg.message = message;
            return this;
        }

        public Builder responseTime(String responseTime) {
            msg.responseTime = responseTime;
            return this;
        }

        public Builder addDto(Object dto) {
            if (dto != null) {
                msg.data.put(dto.getClass().getSimpleName(), dto);
            }
            return this;
        }

        public Builder addDtoList(List<?> dtoList) {
            if (dtoList != null && !dtoList.isEmpty()) {
                Object first = dtoList.get(0);
                if (first != null) {
                    msg.data.put(first.getClass().getSimpleName(), dtoList);
                }
            }
            return this;
        }

        public Builder put(String key, Object value) {
            if (key != null && value != null) {
                msg.data.put(key, value);
            }
            return this;
        }

        public BaseMessage build() {
            return msg;
        }
    }

    // ==================== 数据存取 ====================

    /**
     * 按自定义 key 放入数据。
     */
    public BaseMessage put(String key, Object value) {
        if (key != null && value != null) {
            this.data.put(key, value);
        }
        return this;
    }

    /**
     * 按 key + 类型取出单个对象。
     */
    public <T> T get(String key, Class<T> clazz) {
        Object obj = this.data.get(key);
        if (obj == null) {
            return null;
        }
        if (clazz.isInstance(obj)) {
            return clazz.cast(obj);
        }
        try {
            return objectMapper.readValue(toJsonString(obj), clazz);
        } catch (Exception e) {
            throw new RuntimeException("BaseMessage 反序列化失败: key=" + key, e);
        }
    }

    /**
     * 按 key + 类型取出对象列表。
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> clazz) {
        Object obj = this.data.get(key);
        if (obj == null) {
            return null;
        }
        if (obj instanceof List) {
            return (List<T>) obj;
        }
        try {
            JavaType javaType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            return objectMapper.readValue(toJsonString(obj), javaType);
        } catch (Exception e) {
            throw new RuntimeException("BaseMessage 列表反序列化失败: key=" + key, e);
        }
    }

    /**
     * 添加单个对象（按类名作为 key）。
     */
    public void addDto(Object dto) {
        if (dto == null) {
            return;
        }
        this.data.put(dto.getClass().getSimpleName(), dto);
    }

    /**
     * 添加对象列表（按首个元素类名作为 key）。
     */
    public void addDtoList(List<?> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }
        Object first = dtoList.get(0);
        if (first != null) {
            this.data.put(first.getClass().getSimpleName(), dtoList);
        }
    }

    /**
     * 获取单个对象（按类名作为 key）。
     */
    public <T> T getDto(Class<T> clazz) {
        return get(clazz.getSimpleName(), clazz);
    }

    /**
     * 获取对象列表（按类名作为 key）。
     */
    public <T> List<T> getDtoList(Class<T> clazz) {
        return getList(clazz.getSimpleName(), clazz);
    }

    private static String toJsonString(Object obj) throws JsonProcessingException {
        if (obj instanceof String) {
            return (String) obj;
        }
        return objectMapper.writeValueAsString(obj);
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
