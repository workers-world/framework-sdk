package com.books.framework.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class JsonRowDiffUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<String> DEFAULT_SKIP_SUFFIXES = Set.of("_encrypted", "_hash");

    private static final Set<String> DEFAULT_SKIP_FIELDS = Set.of("tech_trace_id");

    private JsonRowDiffUtil() {
    }

    public static List<JsonFieldDiff> diff(String beforeJson, String afterJson, String changedFieldsJson,
                                           Set<String> extraHiddenFields) {
        Map<String, Object> before = parseJsonMap(beforeJson);
        Map<String, Object> after = parseJsonMap(afterJson);
        Set<String> changedKeys = normalizeChangedFields(changedFieldsJson);
        Set<String> hidden = buildHiddenSet(extraHiddenFields);
        boolean insertOnly = before.isEmpty() && !after.isEmpty();

        LinkedHashSet<String> keys = new LinkedHashSet<>();
        before.keySet().stream().filter(k -> !isHidden(k, hidden)).forEach(keys::add);
        after.keySet().stream().filter(k -> !isHidden(k, hidden)).forEach(keys::add);

        List<JsonFieldDiff> result = new ArrayList<>(keys.size());
        for (String key : keys) {
            Object beforeVal = before.get(key);
            Object afterVal = after.get(key);
            boolean valueChanged = !Objects.equals(stringify(beforeVal), stringify(afterVal));
            boolean markedChanged = changedKeys.contains(key) || changedKeys.contains(snakeToCamel(key));
            boolean changed = insertOnly ? afterVal != null : (valueChanged || markedChanged);
            JsonFieldDiff item = new JsonFieldDiff();
            item.setFieldKey(key);
            item.setBeforeValue(beforeVal);
            item.setAfterValue(afterVal);
            item.setChanged(changed);
            result.add(item);
        }
        return result;
    }

    public static String displayValue(Object value) {
        if (value == null) {
            return "—";
        }
        String s = stringify(value);
        return s.isEmpty() ? "—" : s;
    }

    private static Map<String, Object> parseJsonMap(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private static Set<String> normalizeChangedFields(String changedFieldsJson) {
        if (changedFieldsJson == null || changedFieldsJson.isBlank()) {
            return Collections.emptySet();
        }
        try {
            List<String> fields = OBJECT_MAPPER.readValue(changedFieldsJson, new TypeReference<List<String>>() {
            });
            Set<String> normalized = new LinkedHashSet<>();
            for (String field : fields) {
                if (field == null || field.isBlank()) {
                    continue;
                }
                normalized.add(field);
                normalized.add(camelToSnake(field));
            }
            return normalized;
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    private static Set<String> buildHiddenSet(Set<String> extraHiddenFields) {
        Set<String> hidden = new LinkedHashSet<>(DEFAULT_SKIP_FIELDS);
        if (extraHiddenFields != null) {
            hidden.addAll(extraHiddenFields);
        }
        return hidden;
    }

    private static boolean isHidden(String key, Set<String> hidden) {
        if (hidden.contains(key)) {
            return true;
        }
        for (String suffix : DEFAULT_SKIP_SUFFIXES) {
            if (key.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private static String stringify(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value);
    }

    static String camelToSnake(String camel) {
        if (camel == null || camel.isBlank()) {
            return camel;
        }
        if (camel.contains("_")) {
            return camel;
        }
        StringBuilder sb = new StringBuilder(camel.length() + 4);
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String snakeToCamel(String snake) {
        if (snake == null || !snake.contains("_")) {
            return snake;
        }
        StringBuilder sb = new StringBuilder();
        boolean upperNext = false;
        for (int i = 0; i < snake.length(); i++) {
            char c = snake.charAt(i);
            if (c == '_') {
                upperNext = true;
                continue;
            }
            if (upperNext) {
                sb.append(Character.toUpperCase(c));
                upperNext = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static class JsonFieldDiff {
        private String fieldKey;
        private Object beforeValue;
        private Object afterValue;
        private boolean changed;

        public String getFieldKey() {
            return fieldKey;
        }

        public void setFieldKey(String fieldKey) {
            this.fieldKey = fieldKey;
        }

        public Object getBeforeValue() {
            return beforeValue;
        }

        public void setBeforeValue(Object beforeValue) {
            this.beforeValue = beforeValue;
        }

        public Object getAfterValue() {
            return afterValue;
        }

        public void setAfterValue(Object afterValue) {
            this.afterValue = afterValue;
        }

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }
    }
}
