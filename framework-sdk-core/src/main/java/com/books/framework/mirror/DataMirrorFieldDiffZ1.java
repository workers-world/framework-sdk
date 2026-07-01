package com.books.framework.mirror;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataMirrorFieldDiffZ1 {
    private String fieldKey;
    private String label;
    private String beforeDisplay;
    private String afterDisplay;
    private boolean changed;
}
