package com.books.framework.progress;

import java.util.List;

public class CompositeProgressReporter implements ProgressReporter {

    private final List<ProgressReporter> delegates;

    public CompositeProgressReporter(List<ProgressReporter> delegates) {
        this.delegates = delegates == null ? List.of() : List.copyOf(delegates);
    }

    @Override
    public void report(ProgressSnapshot snapshot) {
        for (ProgressReporter delegate : delegates) {
            delegate.report(snapshot);
        }
    }
}
