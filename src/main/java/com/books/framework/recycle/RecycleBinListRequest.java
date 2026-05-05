package com.books.framework.recycle;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecycleBinListRequest {
    private String tableName;
    private Integer page;
    private Integer limit;
}
