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
public class DataMirrorBatchItemZ1 {
    private Long id;
    private String tableName;
    private Long recordId;
    private String operation;
    private String changeReason;
    private String changedFields;
    private LocalDateTime createdAt;
    private List<DataMirrorFieldDiffZ1> fieldDiffs;
}
