package com.books.framework.mirror;

import lombok.Data;

@Data
public class DataMirrorQuery {
    private String tableName;
    private String bizKey;
    private String operation;
    private String changeReason;
}
