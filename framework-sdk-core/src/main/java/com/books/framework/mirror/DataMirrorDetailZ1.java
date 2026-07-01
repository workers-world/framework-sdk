package com.books.framework.mirror;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMirrorDetailZ1 {
    private Long id;
    private String tableName;
    private Long recordId;
    private String bizKey;
    private String operation;
    private String changedFields;
    private String changeReason;
    private String batchId;
    private String traceId;
    private LocalDateTime createdAt;
    private List<DataMirrorFieldDiffZ1> fieldDiffs;
    private List<DataMirrorListItemZ1> batchEntries;
}
