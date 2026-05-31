package com.books.framework.progress;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ProgressRunner {

    private final ProgressContext progressContext;

    public ProgressRunner(ProgressContext progressContext) {
        this.progressContext = progressContext;
    }

    public <T> void forEach(List<T> items, String phase, Consumer<T> action) {
        if (items == null || items.isEmpty()) {
            progressContext.tick(phase, 0, 0);
            return;
        }
        int total = items.size();
        progressContext.tick(phase, 0, total);
        for (int i = 0; i < items.size(); i++) {
            action.accept(items.get(i));
            progressContext.tick(phase, i + 1, total);
        }
    }

    public <T> void forEachIndexed(List<T> items, String phase, BiConsumer<Integer, T> action) {
        if (items == null || items.isEmpty()) {
            progressContext.tick(phase, 0, 0);
            return;
        }
        int total = items.size();
        progressContext.tick(phase, 0, total);
        for (int i = 0; i < items.size(); i++) {
            action.accept(i, items.get(i));
            progressContext.tick(phase, i + 1, total);
        }
    }
}
