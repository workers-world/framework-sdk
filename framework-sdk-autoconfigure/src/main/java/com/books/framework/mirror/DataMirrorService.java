package com.books.framework.mirror;

import com.books.framework.manager.DataMirrorManager;
import com.books.framework.trace.TraceIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

public class DataMirrorService {

    private static final Logger log = LoggerFactory.getLogger(DataMirrorService.class);
    private static final Pattern TABLE_NAME_RE = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]{0,63}$");

    private final DataMirrorManager manager;

    public DataMirrorService(DataMirrorManager manager) {
        this.manager = manager;
    }

    public String snapshotRow(String tableName, Long recordId) {
        validateTableName(tableName);
        if (recordId == null) {
            throw new IllegalArgumentException("recordId 不能为空");
        }
        Map<String, Object> row = manager.queryForMap("SELECT * FROM " + tableName + " WHERE id = ?", recordId);
        return manager.serializeRow(row);
    }

    public DataMirrorLogEntry recordInsert(String tableName, Long recordId, String bizKey,
                                           String afterJson, String changeReason, String batchId) {
        validateTableName(tableName);
        if (recordId == null) {
            throw new IllegalArgumentException("recordId 不能为空");
        }
        DataMirrorLogEntry entry = manager.insertEntry(
                tableName,
                recordId,
                bizKey,
                MirrorOperation.INSERT.name(),
                null,
                afterJson,
                null,
                batchId,
                changeReason,
                currentTraceId()
        );
        log.info("镜像 INSERT: {} id={} batch={}", tableName, recordId, batchId);
        return entry;
    }

    public DataMirrorLogEntry recordUpdate(String tableName, Long recordId, String bizKey,
                                           String beforeJson, String afterJson, String changedFieldsJson,
                                           String changeReason, String batchId) {
        validateTableName(tableName);
        if (recordId == null) {
            throw new IllegalArgumentException("recordId 不能为空");
        }
        DataMirrorLogEntry entry = manager.insertEntry(
                tableName,
                recordId,
                bizKey,
                MirrorOperation.UPDATE.name(),
                beforeJson,
                afterJson,
                changedFieldsJson,
                batchId,
                changeReason,
                currentTraceId()
        );
        log.info("镜像 UPDATE: {} id={} batch={}", tableName, recordId, batchId);
        return entry;
    }

    private void validateTableName(String tableName) {
        if (tableName == null || !TABLE_NAME_RE.matcher(tableName).matches()) {
            throw new IllegalArgumentException("非法表名: " + tableName);
        }
    }

    private String currentTraceId() {
        String traceId = TraceIdUtils.get();
        return traceId;
    }
}
