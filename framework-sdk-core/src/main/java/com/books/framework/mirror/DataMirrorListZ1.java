package com.books.framework.mirror;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMirrorListZ1 {
    private List<DataMirrorListItemZ1> list;
    private long total;
    private int page;
    private int limit;
}
