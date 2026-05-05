package com.books.framework.recycle;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认恢复策略：解析 data_json → 动态构建 INSERT，排除 id 列让数据库重新分配主键。
 */
public class DefaultRestoreHandler implements RestoreHandler {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public boolean supports(String tableName) {
        return true;
    }

    @Override
    public Long restoreSingle(RecycleBinEntry entry, JdbcTemplate jdbc) {
        if (entry.getDataJson() == null || entry.getDataJson().trim().isEmpty()) {
            throw new IllegalArgumentException("data_json 为空，无法恢复");
        }
        Map<String, Object> map;
        try {
            map = OBJECT_MAPPER.readValue(entry.getDataJson(), new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("解析 data_json 失败: " + e.getMessage(), e);
        }
        if (map.isEmpty()) {
            throw new IllegalArgumentException("data_json 无有效字段");
        }

        List<String> columns = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if ("id".equals(e.getKey())) continue;
            columns.add(e.getKey());
            values.add(e.getValue());
        }
        if (columns.isEmpty()) {
            throw new IllegalArgumentException("data_json 除 id 外无其他字段");
        }

        String sql = buildInsert(entry.getTableName(), columns);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.size(); i++) {
                ps.setObject(i + 1, values.get(i));
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    @Override
    public int restoreBatch(List<RecycleBinEntry> entries, JdbcTemplate jdbc) {
        int count = 0;
        for (RecycleBinEntry entry : entries) {
            restoreSingle(entry, jdbc);
            count++;
        }
        return count;
    }

    private String buildInsert(String tableName, List<String> columns) {
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(tableName).append(" (");
        sql.append(String.join(", ", columns));
        sql.append(") VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(")");
        return sql.toString();
    }
}
