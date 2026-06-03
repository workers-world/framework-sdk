package com.books.framework.recycle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecycleBinListZ1 {
    private List<RecycleBinEntry> list;
    private long total;
    private int page;
    private int limit;
}
