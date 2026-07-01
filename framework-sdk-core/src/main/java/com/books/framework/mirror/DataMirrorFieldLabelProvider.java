package com.books.framework.mirror;

import java.util.Collections;
import java.util.Set;

public interface DataMirrorFieldLabelProvider {

    boolean supports(String tableName);

    String label(String tableName, String fieldKey);

    default Set<String> hiddenFields(String tableName) {
        return Collections.emptySet();
    }
}
