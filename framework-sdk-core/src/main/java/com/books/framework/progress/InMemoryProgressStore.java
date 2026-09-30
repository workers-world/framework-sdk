package com.books.framework.progress;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProgressStore implements ProgressStore {

    private final Map<String, ProgressSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public void save(ProgressSnapshot snapshot) {
        if (snapshot == null || snapshot.getTaskId() == null) {
            return;
        }
        snapshots.put(snapshot.getTaskId(), snapshot);
    }

    @Override
    public Optional<ProgressSnapshot> get(String taskId) {
        if (taskId == null || taskId.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(snapshots.get(taskId));
    }
}
