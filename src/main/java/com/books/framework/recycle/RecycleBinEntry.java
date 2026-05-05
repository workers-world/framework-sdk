package com.books.framework.recycle;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RecycleBinEntry {
    private Long id;
    private String tableName;
    private Long originalId;
    private String batchId;
    private String dataJson;
    private String deleteReason;
    private LocalDateTime deletedAt;
}
