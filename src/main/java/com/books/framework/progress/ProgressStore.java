package com.books.framework.progress;

import java.util.Optional;

public interface ProgressStore {

    void save(ProgressSnapshot snapshot);

    Optional<ProgressSnapshot> get(String taskId);
}
