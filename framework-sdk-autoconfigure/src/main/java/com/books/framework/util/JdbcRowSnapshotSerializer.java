package com.books.framework.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JdbcRowSnapshotSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JdbcRowSnapshotSerializer() {
    }

    public static String serializeRow(Map<String, Object> row) {
        Map<String, Object> cleaned = new LinkedHashMap<>(row.size());
        for (Map.Entry<String, Object> e : row.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Timestamp) {
                v = v.toString();
            } else if (v instanceof java.sql.Date) {
                v = v.toString();
            } else if (v instanceof java.sql.Time) {
                v = v.toString();
            } else if (v instanceof byte[]) {
                v = Base64.getEncoder().encodeToString((byte[]) v);
            }
            cleaned.put(e.getKey(), v);
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(cleaned);
        } catch (Exception ex) {
            throw new RuntimeException("序列化记录失败", ex);
        }
    }
}
