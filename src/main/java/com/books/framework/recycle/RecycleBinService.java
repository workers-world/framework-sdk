package com.books.framework.recycle;

import com.books.framework.manager.RecycleBinManager;
import com.books.framework.utils.PagingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 回收站业务编排层——只做流程调度，数据访问委托给 {@link RecycleBinManager}。
 */
public class RecycleBinService {

    private static final Logger log = LoggerFactory.getLogger(RecycleBinService.class);
    private static final Pattern TABLE_NAME_RE = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,63}$");

    private final RecycleBinManager manager;
    private final List<RestoreHandler> restoreHandlers;

    public RecycleBinService(RecycleBinManager manager, List<RestoreHandler> restoreHandlers) {
        this.manager = manager;
        this.restoreHandlers = restoreHandlers;
    }

    /**
     * 软删除单条记录：查询原表 → 序列化 JSON → 写入回收站 → 删除原记录
     */
    @Transactional
    public RecycleBinEntry softDelete(String tableName, Long originalId, String deleteReason) {
        validateTableName(tableName);
        Map<String, Object> row = manager.queryForMap("SELECT * FROM " + tableName + " WHERE id = ?", originalId);
        String dataJson = manager.serializeRow(row);
        String batchId = UUID.randomUUID().toString();
        RecycleBinEntry entry = manager.insertEntry(tableName, originalId, dataJson, batchId, deleteReason);
        manager.update("DELETE FROM " + tableName + " WHERE id = ?", originalId);
        log.info("软删除: {} id={} → batch={}", tableName, originalId, batchId);
        return entry;
    }

    /**
     * 批量保存快照（调用方自行处理原表删除），共享同一 batchId
     */
    public void saveBatchSnapshots(String tableName, List<Long> originalIds, List<String> dataJsons,
                                   String batchId, String deleteReason) {
        validateTableName(tableName);
        manager.insertBatch(tableName, originalIds, dataJsons, batchId, deleteReason);
        log.info("批量快照: {} x{} → batch={}", tableName, originalIds.size(), batchId);
    }

    /**
     * 分页查询回收站列表
     */
    public RecycleBinListZ1 list(RecycleBinListX1 x1) {
        int p = PagingUtils.clampPage(x1.getPage());
        int l = PagingUtils.clampLimit(x1.getLimit() != null ? x1.getLimit() : 20, 20, 100);
        long total = manager.count(x1.getTableName());
        List<RecycleBinEntry> list = manager.findPage(x1.getTableName(), p, l);
        return RecycleBinListZ1.builder().list(list).total(total).page(p).limit(l).build();
    }

    public RecycleBinEntry getById(Long id) {
        return manager.findById(id);
    }

    public List<RecycleBinEntry> getByBatchId(String batchId) {
        return manager.findByBatchId(batchId);
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
        Long newId = handler.restoreSingle(entry, manager);
        manager.deleteById(id);
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
        int count = handler.restoreBatch(entries, manager);
        manager.deleteByBatchId(batchId);
        log.info("恢复批次: {} batch={} 共 {} 条", tableName, batchId, count);
        return count;
    }

    public void permanentlyDelete(Long id) {
        manager.deleteById(id);
    }

    public int permanentlyDeleteBatch(String batchId) {
        int deleted = manager.deleteByBatchId(batchId);
        log.info("物理清除批次: {} 共 {} 条", batchId, deleted);
        return deleted;
    }

    public int cleanupOlderThan(int days) {
        int deleted = manager.deleteOlderThan(days);
        if (deleted > 0) {
            log.info("自动清理: 删除 {} 天前的 {} 条记录", days, deleted);
        }
        return deleted;
    }

    // ===== 内部 =====

    private void validateTableName(String tableName) {
        if (tableName == null || !TABLE_NAME_RE.matcher(tableName).matches()) {
            throw new IllegalArgumentException("非法表名: " + tableName);
        }
    }

    private RestoreHandler findHandler(String tableName) {
        for (RestoreHandler h : restoreHandlers) {
            if (h.supports(tableName)) return h;
        }
        throw new IllegalStateException("没有找到表 " + tableName + " 的恢复策略");
    }
}
