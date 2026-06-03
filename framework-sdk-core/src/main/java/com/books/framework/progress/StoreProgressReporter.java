package com.books.framework.progress;

public class StoreProgressReporter implements ProgressReporter {

    private final ProgressStore progressStore;

    public StoreProgressReporter(ProgressStore progressStore) {
        this.progressStore = progressStore;
    }

    @Override
    public void report(ProgressSnapshot snapshot) {
        progressStore.save(snapshot);
    }
}
