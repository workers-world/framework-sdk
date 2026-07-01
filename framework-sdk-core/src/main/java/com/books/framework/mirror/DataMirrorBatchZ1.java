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
public class DataMirrorBatchZ1 {
    private String batchId;
    private List<DataMirrorBatchItemZ1> entries;
}
