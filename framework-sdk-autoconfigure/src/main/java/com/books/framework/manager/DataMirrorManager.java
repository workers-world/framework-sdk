package com.books.framework.manager;

import com.books.framework.mirror.DataMirrorLogEntry;
import com.books.framework.mirror.DataMirrorQuery;
import com.books.framework.util.JdbcRowSnapshotSerializer;
import com.books.framework.utils.PagingUtils;
import jakarta.annotation.PostConstruct;
import org.intellij.lang.annotations.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataMirrorManager {

    private static final Logger log = LoggerFactory.getLogger(DataMirrorManager.class);

    private final JdbcTemplate jdbc;

    public DataMirrorManager(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void initTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS data_mirror_log (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "table_name VARCHAR(64) NOT NULL, " +
                    "record_id BIGINT NOT NULL, " +
                    "biz_key VARCHAR(128), " +
                    "operation VARCHAR(16) NOT NULL, " +
                    "before_json LONGTEXT, " +
                    "after_json LONGTEXT, " +
                    "changed_fields VARCHAR(1000), " +
                    "batch_id VARCHAR(64), " +
                    "change_reason VARCHAR(200), " +
                    "trace_id VARCHAR(64), " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "INDEX idx_dm_table (table_name), " +
                    "INDEX idx_dm_record (table_name, record_id), " +
                    "INDEX idx_dm_biz (biz_key), " +
                    "INDEX idx_dm_batch (batch_id)" +
                    ")");
            log.info("data_mirror_log 表初始化完成");
        } catch (Exception e) {
            log.warn("data_mirror_log 表初始化失败(数据库未就绪,稍后重试): {}", e.getMessage());
        }
    }

    public Map<String, Object> queryForMap(@Language("SQL") String sql, Object... params) {
        return jdbc.queryForMap(sql, params);
    }

    public String serializeRow(Map<String, Object> row) {
        return JdbcRowSnapshotSerializer.serializeRow(row);
    }

    public DataMirrorLogEntry insertEntry(String tableName, Long recordId, String bizKey, String operation,
                                          String beforeJson, String afterJson, String changedFields,
                                          String batchId, String changeReason, String traceId) {
        String sql = "INSERT INTO data_mirror_log (table_name, record_id, biz_key, operation, before_json, after_json, " +
                "changed_fields, batch_id, change_reason, trace_id, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        long id = insertReturningKey(sql, new Object[]{
                tableName, recordId, bizKey, operation, beforeJson, afterJson,
                changedFields, batchId, changeReason, traceId
        });

        DataMirrorLogEntry entry = new DataMirrorLogEntry();
        entry.setId(id);
        entry.setTableName(tableName);
        entry.setRecordId(recordId);
        entry.setBizKey(bizKey);
        entry.setOperation(operation);
        entry.setBeforeJson(beforeJson);
        entry.setAfterJson(afterJson);
        entry.setChangedFields(changedFields);
        entry.setBatchId(batchId);
        entry.setChangeReason(changeReason);
        entry.setTraceId(traceId);
        entry.setCreatedAt(LocalDateTime.now());
        return entry;
    }

    public List<DataMirrorLogEntry> findByBatchId(String batchId) {
        return jdbc.query(
                "SELECT * FROM data_mirror_log WHERE batch_id = ? ORDER BY created_at ASC, id ASC",
                rowMapper(),
                batchId
        );
    }

    public long count(DataMirrorQuery query) {
        QuerySql sql = buildQuerySql(query, true);
        Long total = jdbc.queryForObject(sql.sql, Long.class, sql.params.toArray());
        return total != null ? total : 0L;
    }

    public List<DataMirrorLogEntry> findPage(DataMirrorQuery query, int page, int limit) {
        int offset = PagingUtils.offset(page, limit);
        QuerySql sql = buildQuerySql(query, false);
        String pageSql = sql.sql + " ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?";
        List<Object> params = new ArrayList<>(sql.params);
        params.add(limit);
        params.add(offset);
        return jdbc.query(pageSql, rowMapper(), params.toArray());
    }

    public DataMirrorLogEntry findById(Long id) {
        if (id == null) {
            return null;
        }
        List<DataMirrorLogEntry> list = jdbc.query(
                "SELECT * FROM data_mirror_log WHERE id = ?",
                rowMapper(),
                id
        );
        return list.isEmpty() ? null : list.get(0);
    }

    private QuerySql buildQuerySql(DataMirrorQuery query, boolean countOnly) {
        StringBuilder sb = new StringBuilder(countOnly ? "SELECT COUNT(1) FROM data_mirror_log WHERE 1=1" : "SELECT * FROM data_mirror_log WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (query != null) {
            if (query.getTableName() != null && !query.getTableName().isBlank()) {
                sb.append(" AND table_name = ?");
                params.add(query.getTableName().trim());
            }
            if (query.getBizKey() != null && !query.getBizKey().isBlank()) {
                sb.append(" AND biz_key = ?");
                params.add(query.getBizKey().trim());
            }
            if (query.getOperation() != null && !query.getOperation().isBlank()) {
                sb.append(" AND operation = ?");
                params.add(query.getOperation().trim());
            }
            if (query.getChangeReason() != null && !query.getChangeReason().isBlank()) {
                sb.append(" AND change_reason LIKE ?");
                params.add("%" + query.getChangeReason().trim() + "%");
            }
        }
        QuerySql result = new QuerySql();
        result.sql = sb.toString();
        result.params = params;
        return result;
    }

    private static class QuerySql {
        private String sql;
        private List<Object> params;
    }

    private long insertReturningKey(@Language("SQL") String sql, Object[] params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : 0L;
    }

    private RowMapper<DataMirrorLogEntry> rowMapper() {
        return (rs, rowNum) -> {
            DataMirrorLogEntry e = new DataMirrorLogEntry();
            e.setId(rs.getLong("id"));
            e.setTableName(rs.getString("table_name"));
            e.setRecordId(rs.getLong("record_id"));
            e.setBizKey(rs.getString("biz_key"));
            e.setOperation(rs.getString("operation"));
            e.setBeforeJson(rs.getString("before_json"));
            e.setAfterJson(rs.getString("after_json"));
            e.setChangedFields(rs.getString("changed_fields"));
            e.setBatchId(rs.getString("batch_id"));
            e.setChangeReason(rs.getString("change_reason"));
            e.setTraceId(rs.getString("trace_id"));
            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                e.setCreatedAt(ts.toLocalDateTime());
            }
            return e;
        };
    }
}
