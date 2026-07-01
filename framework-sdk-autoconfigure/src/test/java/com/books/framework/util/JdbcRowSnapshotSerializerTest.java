package com.books.framework.util;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcRowSnapshotSerializerTest {

    @Test
    void serializeRowHandlesTimestampAndNull() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", 1L);
        row.put("name", "test");
        row.put("created_at", Timestamp.valueOf("2026-01-01 12:00:00"));
        row.put("payload", null);

        String json = JdbcRowSnapshotSerializer.serializeRow(row);

        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"name\":\"test\""));
        assertTrue(json.contains("2026-01-01 12:00:00"));
    }
}
