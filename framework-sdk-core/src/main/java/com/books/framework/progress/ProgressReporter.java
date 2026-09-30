package com.books.framework.progress;

@FunctionalInterface
public interface ProgressReporter {

    void report(ProgressSnapshot snapshot);
}
