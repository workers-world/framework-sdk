package com.books.framework.mirror;

import lombok.Data;

@Data
public class DataMirrorListX1 {
    private String tableName;
    private String bizKey;
    private String operation;
    private String changeReason;
    private Integer page;
    private Integer limit;
}
