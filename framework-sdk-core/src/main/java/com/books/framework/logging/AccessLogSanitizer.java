package com.books.framework.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class AccessLogSanitizer {

    private static final String MASK_VALUE = "****";

    private final Set<String> maskFields;
    private final int maxBodyLength;
    private final ObjectMapper objectMapper;

    public AccessLogSanitizer(Set<String> maskFields, int maxBodyLength) {
        this.maskFields = normalizeMaskFields(maskFields);
        this.maxBodyLength = maxBodyLength > 0 ? maxBodyLength : 2000;
        this.objectMapper = new ObjectMapper();
    }

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
            objectNode.fields().forEachRemaining(entry -> {
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
