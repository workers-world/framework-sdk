package com.books.framework.mirror;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMirrorListItemZ1 {
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
}
