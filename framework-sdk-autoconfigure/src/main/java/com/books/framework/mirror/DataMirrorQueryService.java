package com.books.framework.mirror;

import com.books.framework.manager.DataMirrorManager;
import com.books.framework.util.JsonRowDiffUtil;
import com.books.framework.utils.PagingUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataMirrorQueryService {

    private final DataMirrorManager manager;
    private final DataMirrorLabelResolver labelResolver;

    public DataMirrorQueryService(DataMirrorManager manager, DataMirrorLabelResolver labelResolver) {
        this.manager = manager;
        this.labelResolver = labelResolver;
    }

    public DataMirrorListZ1 list(DataMirrorListX1 x1) {
        int page = PagingUtils.clampPage(x1.getPage());
        int limit = PagingUtils.clampLimit(x1.getLimit() != null ? x1.getLimit() : 20, 20, 100);
        DataMirrorQuery query = toQuery(x1);
        long total = manager.count(query);
        List<DataMirrorLogEntry> rows = manager.findPage(query, page, limit);
        List<DataMirrorListItemZ1> list = rows.stream().map(this::toListItem).collect(Collectors.toList());
        return DataMirrorListZ1.builder()
                .list(list)
                .total(total)
                .page(page)
                .limit(limit)
                .build();
    }

    public DataMirrorDetailZ1 getDetail(Long id) {
        DataMirrorLogEntry entry = manager.findById(id);
        if (entry == null) {
            throw new IllegalArgumentException("镜像记录不存在: " + id);
        }
        List<DataMirrorFieldDiffZ1> fieldDiffs = buildFieldDiffs(entry);
        List<DataMirrorListItemZ1> batchEntries = buildBatchEntries(entry.getBatchId(), entry.getId());
        return DataMirrorDetailZ1.builder()
                .id(entry.getId())
                .tableName(entry.getTableName())
                .recordId(entry.getRecordId())
                .bizKey(entry.getBizKey())
                .operation(entry.getOperation())
                .changedFields(entry.getChangedFields())
                .changeReason(entry.getChangeReason())
                .batchId(entry.getBatchId())
                .traceId(entry.getTraceId())
                .createdAt(entry.getCreatedAt())
                .fieldDiffs(fieldDiffs)
                .batchEntries(batchEntries)
                .build();
    }

    public DataMirrorBatchZ1 getBatch(String batchId) {
        if (batchId == null || batchId.isBlank()) {
            throw new IllegalArgumentException("batchId 不能为空");
        }
        List<DataMirrorLogEntry> rows = manager.findByBatchId(batchId.trim());
        List<DataMirrorBatchItemZ1> entries = new ArrayList<>(rows.size());
        for (DataMirrorLogEntry row : rows) {
            entries.add(DataMirrorBatchItemZ1.builder()
                    .id(row.getId())
                    .tableName(row.getTableName())
                    .recordId(row.getRecordId())
                    .operation(row.getOperation())
                    .changeReason(row.getChangeReason())
                    .changedFields(row.getChangedFields())
                    .createdAt(row.getCreatedAt())
                    .fieldDiffs(buildFieldDiffs(row))
                    .build());
        }
        return DataMirrorBatchZ1.builder()
                .batchId(batchId.trim())
                .entries(entries)
                .build();
    }

    private List<DataMirrorFieldDiffZ1> buildFieldDiffs(DataMirrorLogEntry entry) {
        Set<String> hidden = labelResolver.resolveHiddenFields(entry.getTableName());
        List<JsonRowDiffUtil.JsonFieldDiff> diffs = JsonRowDiffUtil.diff(
                entry.getBeforeJson(),
                entry.getAfterJson(),
                entry.getChangedFields(),
                hidden
        );
        List<DataMirrorFieldDiffZ1> result = new ArrayList<>(diffs.size());
        for (JsonRowDiffUtil.JsonFieldDiff diff : diffs) {
            String beforeDisplay = JsonRowDiffUtil.displayValue(diff.getBeforeValue());
            String afterDisplay = JsonRowDiffUtil.displayValue(diff.getAfterValue());
            if (isEncryptedField(diff.getFieldKey())) {
                if (!"—".equals(beforeDisplay)) {
                    beforeDisplay = beforeDisplay + "（密文）";
                }
                if (!"—".equals(afterDisplay)) {
                    afterDisplay = afterDisplay + "（密文）";
                }
            }
            result.add(DataMirrorFieldDiffZ1.builder()
                    .fieldKey(diff.getFieldKey())
                    .label(labelResolver.resolveLabel(entry.getTableName(), diff.getFieldKey()))
                    .beforeDisplay(beforeDisplay)
                    .afterDisplay(afterDisplay)
                    .changed(diff.isChanged())
                    .build());
        }
        return result;
    }

    private boolean isEncryptedField(String fieldKey) {
        return fieldKey != null && fieldKey.endsWith("_encrypted");
    }

    private List<DataMirrorListItemZ1> buildBatchEntries(String batchId, Long currentId) {
        if (batchId == null || batchId.isBlank()) {
            return List.of();
        }
        return manager.findByBatchId(batchId).stream()
                .filter(row -> currentId == null || !currentId.equals(row.getId()))
                .map(this::toListItem)
                .collect(Collectors.toList());
    }

    private DataMirrorListItemZ1 toListItem(DataMirrorLogEntry entry) {
        return DataMirrorListItemZ1.builder()
                .id(entry.getId())
                .tableName(entry.getTableName())
                .recordId(entry.getRecordId())
                .bizKey(entry.getBizKey())
                .operation(entry.getOperation())
                .changedFields(entry.getChangedFields())
                .changeReason(entry.getChangeReason())
                .batchId(entry.getBatchId())
                .traceId(entry.getTraceId())
                .createdAt(entry.getCreatedAt())
                .build();
    }

    private DataMirrorQuery toQuery(DataMirrorListX1 x1) {
        DataMirrorQuery query = new DataMirrorQuery();
        if (x1 == null) {
            return query;
        }
        query.setTableName(trimToNull(x1.getTableName()));
        query.setBizKey(trimToNull(x1.getBizKey()));
        query.setOperation(trimToNull(x1.getOperation()));
        query.setChangeReason(trimToNull(x1.getChangeReason()));
        return query;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
