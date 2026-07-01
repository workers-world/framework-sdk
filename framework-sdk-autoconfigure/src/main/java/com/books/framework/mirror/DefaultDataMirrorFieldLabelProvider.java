package com.books.framework.mirror;

import java.util.Collections;
import java.util.Set;

public class DefaultDataMirrorFieldLabelProvider implements DataMirrorFieldLabelProvider {

    @Override
    public boolean supports(String tableName) {
        return true;
    }

    @Override
    public String label(String tableName, String fieldKey) {
        if (fieldKey == null || fieldKey.isBlank()) {
            return fieldKey;
        }
        String[] parts = fieldKey.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    @Override
    public Set<String> hiddenFields(String tableName) {
        return Collections.emptySet();
    }
}
