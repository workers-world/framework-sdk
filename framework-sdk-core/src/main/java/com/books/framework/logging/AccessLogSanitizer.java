package com.books.framework.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** 访问日志 JSON/文本脱敏（字段掩码、长度截断）。 */
public final class AccessLogSanitizer {

    private static final String MASK_VALUE = "****";

    private final Set<String> maskFields;
    private final int maxBodyLength;
    private final ObjectMapper objectMapper;

    /**
     * @param maskFields 需掩码的字段名（大小写不敏感）
     * @param maxBodyLength 输出最大长度，≤0 时默认 2000
     */
    public AccessLogSanitizer(Set<String> maskFields, int maxBodyLength) {
        this.maskFields = normalizeMaskFields(maskFields);
        this.maxBodyLength = maxBodyLength > 0 ? maxBodyLength : 2000;
        this.objectMapper = new ObjectMapper();
    }

    /** 将对象序列化为 JSON 后脱敏并截断。 */
    public String sanitize(Object value) {
        if (value == null) {
            return "";
        }
        try {
            JsonNode node = objectMapper.valueToTree(value);
            maskNode(node);
            String json = objectMapper.writeValueAsString(node);
            return truncate(json);
        } catch (Exception e) {
            return truncate(String.valueOf(value));
        }
    }

    /** 解析 JSON 文本后脱敏；非 JSON 时按纯文本截断。 */
    public String sanitizeText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        try {
            JsonNode node = objectMapper.readTree(text);
            maskNode(node);
            return truncate(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            return truncate(text);
        }
    }

    private void maskNode(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.properties().forEach(entry -> {
                if (shouldMask(entry.getKey())) {
                    objectNode.put(entry.getKey(), MASK_VALUE);
                } else {
                    maskNode(entry.getValue());
                }
            });
            return;
        }
        if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode child : arrayNode) {
                maskNode(child);
            }
        }
    }

    private boolean shouldMask(String fieldName) {
        return fieldName != null && maskFields.contains(fieldName.toLowerCase(Locale.ROOT));
    }

    private String truncate(String text) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxBodyLength) {
            return text;
        }
        return text.substring(0, maxBodyLength) + "...(truncated)";
    }

    private static Set<String> normalizeMaskFields(Set<String> maskFields) {
        Set<String> normalized = new HashSet<>();
        if (maskFields == null) {
            return normalized;
        }
        for (String field : maskFields) {
            if (field != null && !field.isBlank()) {
                normalized.add(field.toLowerCase(Locale.ROOT));
            }
        }
        return normalized;
    }
}
