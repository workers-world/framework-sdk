package com.books.framework.recycle;

import com.books.framework.utils.PagingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class RecycleBinService {

    private static final Logger log = LoggerFactory.getLogger(RecycleBinService.class);
    private static final Pattern TABLE_NAME_RE = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,63}$");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JdbcTemplate jdbc;
    private final List<RestoreHandler> restoreHandlers;

    public RecycleBinService(JdbcTemplate jdbc, List<RestoreHandler> restoreHandlers) {
        this.jdbc = jdbc;
        this.restoreHandlers = restoreHandlers;
    }

    @PostConstruct
    public void initTable() {
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
    }

    /**
     * 软删除单条记录：查询原表 → 序列化 JSON → 写入回收站 → 删除原记录
     */
    @Transactional
    public RecycleBinEntry softDelete(String tableName, Long originalId, String deleteReason) {
        validateTableName(tableName);
        Map<String, Object> row = jdbc.queryForMap("SELECT * FROM " + tableName + " WHERE id = ?", originalId);
        String dataJson = serializeRow(row);
        String batchId = UUID.randomUUID().toString();
        RecycleBinEntry entry = insertEntry(tableName, originalId, dataJson, batchId, deleteReason);
        jdbc.update("DELETE FROM " + tableName + " WHERE id = ?", originalId);
        log.info("软删除: {} id={} → batch={}", tableName, originalId, batchId);
        return entry;
    }

    /**
     * 批量保存快照（调用方自行处理原表删除），共享同一 batchId
     */
    public void saveBatchSnapshots(String tableName, List<Long> originalIds, List<String> dataJsons,
                                    String batchId, String deleteReason) {
        validateTableName(tableName);
        String sql = "INSERT INTO recycle_bin (table_name, original_id, batch_id, data_json, delete_reason, deleted_at) VALUES (?, ?, ?, ?, ?, NOW())";
        for (int i = 0; i < originalIds.size(); i++) {
            jdbc.update(sql, tableName, originalIds.get(i), batchId, dataJsons.get(i), deleteReason);
        }
        log.info("批量快照: {} x{} → batch={}", tableName, originalIds.size(), batchId);
    }

    /**
     * 分页查询回收站列表
     */
    public Map<String, Object> list(String tableName, Integer page, Integer limit) {
        int p = PagingUtils.clampPage(page);
        int l = PagingUtils.clampLimit(limit, 20, 100);

        StringBuilder where = new StringBuilder();
        List<Object> params = new ArrayList<>();
        if (tableName != null && !tableName.trim().isEmpty()) {
            where.append(" WHERE table_name = ?");
            params.add(tableName.trim());
        }

        long total = jdbc.queryForObject("SELECT COUNT(1) FROM recycle_bin" + where, Long.class, params.toArray());

        params.add(l);
        params.add(PagingUtils.offset(p, l));
        List<RecycleBinEntry> list = jdbc.query(
                "SELECT * FROM recycle_bin" + where + " ORDER BY deleted_at DESC LIMIT ? OFFSET ?",
                rowMapper(), params.toArray());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", p);
        result.put("limit", l);
        return result;
    }

    public RecycleBinEntry getById(Long id) {
        List<RecycleBinEntry> list = jdbc.query(
                "SELECT * FROM recycle_bin WHERE id = ?", rowMapper(), id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<RecycleBinEntry> getByBatchId(String batchId) {
        return jdbc.query("SELECT * FROM recycle_bin WHERE batch_id = ?", rowMapper(), batchId);
    }

    /**
     * 恢复单条记录，成功后从回收站移除
     */
    @Transactional
    public Long restoreSingle(Long id) {
        RecycleBinEntry entry = getById(id);
        if (entry == null) {
            throw new IllegalArgumentException("回收站记录不存在: " + id);
        }
        RestoreHandler handler = findHandler(entry.getTableName());
        Long newId = handler.restoreSingle(entry, jdbc);
        permanentlyDelete(id);
        log.info("恢复单条: {} id={} → 新id={}", entry.getTableName(), entry.getOriginalId(), newId);
        return newId;
    }

    /**
     * 恢复整个批次，成功后从回收站移除
     */
    @Transactional
    public int restoreBatch(String batchId) {
        List<RecycleBinEntry> entries = getByBatchId(batchId);
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("批次不存在: " + batchId);
        }
        String tableName = entries.get(0).getTableName();
        RestoreHandler handler = findHandler(tableName);
        int count = handler.restoreBatch(entries, jdbc);
        permanentlyDeleteBatch(batchId);
        log.info("恢复批次: {} batch={} 共 {} 条", tableName, batchId, count);
        return count;
    }

    public void permanentlyDelete(Long id) {
        jdbc.update("DELETE FROM recycle_bin WHERE id = ?", id);
    }

    public int permanentlyDeleteBatch(String batchId) {
        int deleted = jdbc.update("DELETE FROM recycle_bin WHERE batch_id = ?", batchId);
        log.info("物理清除批次: {} 共 {} 条", batchId, deleted);
        return deleted;
    }

    public int cleanupOlderThan(int days) {
        int deleted = jdbc.update("DELETE FROM recycle_bin WHERE deleted_at < NOW() - INTERVAL ? DAY", days);
        if (deleted > 0) {
            log.info("自动清理: 删除 {} 天前的 {} 条记录", days, deleted);
        }
        return deleted;
    }

    // ===== 内部方法 =====

    private void validateTableName(String tableName) {
        if (tableName == null || !TABLE_NAME_RE.matcher(tableName).matches()) {
            throw new IllegalArgumentException("非法表名: " + tableName);
        }
    }

    private RecycleBinEntry insertEntry(String tableName, Long originalId, String dataJson,
                                         String batchId, String deleteReason) {
        String sql = "INSERT INTO recycle_bin (table_name, original_id, batch_id, data_json, delete_reason, deleted_at) VALUES (?, ?, ?, ?, ?, NOW())";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tableName);
            ps.setLong(2, originalId);
            ps.setString(3, batchId);
            ps.setString(4, dataJson);
            ps.setString(5, deleteReason);
            return ps;
        }, keyHolder);
        long id = keyHolder.getKey().longValue();

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

    private String serializeRow(Map<String, Object> row) {
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

    private RestoreHandler findHandler(String tableName) {
        for (RestoreHandler h : restoreHandlers) {
            if (h.supports(tableName)) return h;
        }
        throw new IllegalStateException("没有找到表 " + tableName + " 的恢复策略");
    }

    private RowMapper<RecycleBinEntry> rowMapper() {
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
