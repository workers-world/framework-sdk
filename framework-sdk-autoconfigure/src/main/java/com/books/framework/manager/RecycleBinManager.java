package com.books.framework.manager;

import com.books.framework.recycle.RecycleBinEntry;
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
import java.util.List;
import java.util.Map;

/**
 * 回收站数据访问层——所有 JDBC 操作集中在此，不参与业务编排。
 */
public class RecycleBinManager {

    private static final Logger log = LoggerFactory.getLogger(RecycleBinManager.class);

    private final JdbcTemplate jdbc;

    public RecycleBinManager(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @PostConstruct
    public void initTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS recycle_bin (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "table_name VARCHAR(64) NOT NULL, " +
                    "original_id BIGINT NOT NULL, " +
                    "batch_id VARCHAR(64), " +
                    "data_json LONGTEXT NOT NULL, " +
                    "delete_reason VARCHAR(200), " +
                    "deleted_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "INDEX idx_rb_table (table_name), " +
                    "INDEX idx_rb_batch (batch_id)" +
                    ")");
            log.info("recycle_bin 表初始化完成");
        } catch (Exception e) {
            log.warn("recycle_bin 表初始化失败(数据库未就绪,稍后重试): {}", e.getMessage());
        }
    }

    // ===== 通用数据访问（供 Service 编排 + Handler 恢复使用） =====

    /**
     * 查询单行 Map，用于 softDelete 时获取快照
     */
    public Map<String, Object> queryForMap(@Language("SQL") String sql, Object... params) {
        return jdbc.queryForMap(sql, params);
    }

    /**
     * 通用 INSERT 并返回自增主键
     */
    public long insertReturningKey(@Language("SQL") String sql, Object[] params) {
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

    /**
     * 通用 UPDATE / DELETE
     */
    public int update(@Language("SQL") String sql, Object... params) {
        return jdbc.update(sql, params);
    }

    /**
     * 通用查询
     */
    public <T> List<T> query(@Language("SQL") String sql, RowMapper<T> mapper, Object... params) {
        return jdbc.query(sql, mapper, params);
    }

    /**
     * 通用标量查询
     */
    public <T> T queryForObject(@Language("SQL") String sql, Class<T> type, Object... params) {
        return jdbc.queryForObject(sql, type, params);
    }

    // ===== 回收站专用操作 =====

    public RecycleBinEntry insertEntry(String tableName, Long originalId, String dataJson,
                                       String batchId, String deleteReason) {
        String sql = "INSERT INTO recycle_bin (table_name, original_id, batch_id, data_json, delete_reason, deleted_at) VALUES (?, ?, ?, ?, ?, NOW())";
        long id = insertReturningKey(sql, new Object[]{tableName, originalId, batchId, dataJson, deleteReason});

        RecycleBinEntry entry = new RecycleBinEntry();
        entry.setId(id);
        entry.setTableName(tableName);
        entry.setOriginalId(originalId);
        entry.setBatchId(batchId);
        entry.setDataJson(dataJson);
        entry.setDeleteReason(deleteReason);
        entry.setDeletedAt(LocalDateTime.now());
        return entry;
    }

    public void insertBatch(String tableName, List<Long> originalIds, List<String> dataJsons,
                            String batchId, String deleteReason) {
        String sql = "INSERT INTO recycle_bin (table_name, original_id, batch_id, data_json, delete_reason, deleted_at) VALUES (?, ?, ?, ?, ?, NOW())";
        for (int i = 0; i < originalIds.size(); i++) {
            jdbc.update(sql, tableName, originalIds.get(i), batchId, dataJsons.get(i), deleteReason);
        }
    }

    // ===== 查询 =====

    public long count(String tableName) {
        if (tableName != null && !tableName.trim().isEmpty()) {
            return queryForObject("SELECT COUNT(1) FROM recycle_bin WHERE table_name = ?", Long.class, tableName.trim());
        }
        return queryForObject("SELECT COUNT(1) FROM recycle_bin", Long.class);
    }

    public List<RecycleBinEntry> findPage(String tableName, int page, int limit) {
        int offset = PagingUtils.offset(page, limit);
        if (tableName != null && !tableName.trim().isEmpty()) {
            return query(
                    "SELECT * FROM recycle_bin WHERE table_name = ? ORDER BY deleted_at DESC LIMIT ? OFFSET ?",
                    rowMapper(), tableName.trim(), limit, offset);
        }
        return query(
                "SELECT * FROM recycle_bin ORDER BY deleted_at DESC LIMIT ? OFFSET ?",
                rowMapper(), limit, offset);
    }

    public RecycleBinEntry findById(Long id) {
        List<RecycleBinEntry> list = query("SELECT * FROM recycle_bin WHERE id = ?", rowMapper(), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<RecycleBinEntry> findByBatchId(String batchId) {
        return query("SELECT * FROM recycle_bin WHERE batch_id = ?", rowMapper(), batchId);
    }

    // ===== 删除 =====

    public int deleteById(Long id) {
        return update("DELETE FROM recycle_bin WHERE id = ?", id);
    }

    public int deleteByBatchId(String batchId) {
        return update("DELETE FROM recycle_bin WHERE batch_id = ?", batchId);
    }

    public int deleteOlderThan(int days) {
        return update("DELETE FROM recycle_bin WHERE deleted_at < NOW() - INTERVAL ? DAY", days);
    }

    // ===== 序列化 =====

    /**
     * 将 JDBC 查询结果序列化为 JSON，自动处理 Timestamp/Date/Time/byte[]
     */
    public String serializeRow(Map<String, Object> row) {
        return JdbcRowSnapshotSerializer.serializeRow(row);
    }

    // ===== RowMapper =====

    public RowMapper<RecycleBinEntry> rowMapper() {
        return (rs, rowNum) -> {
            RecycleBinEntry e = new RecycleBinEntry();
            e.setId(rs.getLong("id"));
            e.setTableName(rs.getString("table_name"));
            e.setOriginalId(rs.getLong("original_id"));
            e.setBatchId(rs.getString("batch_id"));
            e.setDataJson(rs.getString("data_json"));
            e.setDeleteReason(rs.getString("delete_reason"));
            Timestamp ts = rs.getTimestamp("deleted_at");
            if (ts != null) {
                e.setDeletedAt(ts.toLocalDateTime());
            }
            return e;
        };
    }
}
