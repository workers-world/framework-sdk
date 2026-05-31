package com.books.framework.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressSnapshot {

    private String taskId;
    private String taskType;
    private String phase;
    private Integer processed;
    private Integer total;
    private ProgressStatus status;
    private String message;
    private long updatedAtMs;
}
