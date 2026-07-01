package com.books.framework.mirror;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataMirrorLabelResolver {

    private final List<DataMirrorFieldLabelProvider> providers;
    private final DefaultDataMirrorFieldLabelProvider defaultProvider;

    public DataMirrorLabelResolver(List<DataMirrorFieldLabelProvider> providers,
                                   DefaultDataMirrorFieldLabelProvider defaultProvider) {
        this.providers = providers != null ? providers : List.of();
        this.defaultProvider = defaultProvider;
    }

    public String resolveLabel(String tableName, String fieldKey) {
        for (DataMirrorFieldLabelProvider provider : providers) {
            if (provider.supports(tableName)) {
                String label = provider.label(tableName, fieldKey);
                if (label != null && !label.isBlank()) {
                    return label;
                }
            }
        }
        return defaultProvider.label(tableName, fieldKey);
    }

    public Set<String> resolveHiddenFields(String tableName) {
        Set<String> hidden = new HashSet<>(defaultProvider.hiddenFields(tableName));
        for (DataMirrorFieldLabelProvider provider : providers) {
            if (provider.supports(tableName)) {
                hidden.addAll(provider.hiddenFields(tableName));
            }
        }
        return hidden.isEmpty() ? Collections.emptySet() : hidden;
    }
}
