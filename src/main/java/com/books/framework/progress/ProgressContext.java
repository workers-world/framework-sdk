package com.books.framework.progress;

public class ProgressContext {

    private static final ThreadLocal<TaskState> CURRENT = new ThreadLocal<>();

    private final ProgressReporter progressReporter;

    public ProgressContext(ProgressReporter progressReporter) {
        this.progressReporter = progressReporter;
    }

    public String bind(String taskType, String taskId, Integer total, int minIntervalMs) {
        TaskState state = new TaskState(taskType, taskId, total, minIntervalMs);
        CURRENT.set(state);
        publish(state, "", 0, total, ProgressStatus.RUNNING, null);
        return taskId;
    }

    public void unbind() {
        CURRENT.remove();
    }

    public void tick(String phase, int processed, Integer total) {
        TaskState state = CURRENT.get();
        if (state == null) {
            return;
        }
        if (total != null) {
            state.total = total;
        }
        state.phase = phase == null ? "" : phase;
        long now = System.currentTimeMillis();
        boolean force = state.total != null && state.total >= 0 && processed >= state.total;
        if (!force && processed != 0) {
            if (now - state.lastSentMs < state.minIntervalMs) {
                if (state.total == null || processed < state.total) {
                    return;
                }
            }
        }
        state.lastSentMs = now;
        state.processed = processed;
        publish(state, state.phase, processed, state.total, ProgressStatus.RUNNING, null);
    }

    public void complete(String phase, int processed, Integer total) {
        TaskState state = CURRENT.get();
        if (state == null) {
            return;
        }
        if (total != null) {
            state.total = total;
        }
        state.processed = processed;
        state.phase = phase == null ? "" : phase;
        publish(state, state.phase, processed, state.total, ProgressStatus.COMPLETED, null);
    }

    public void fail(String message) {
        TaskState state = CURRENT.get();
        if (state == null) {
            return;
        }
        publish(state, state.phase, state.processed, state.total, ProgressStatus.FAILED, message);
    }

    public static void runInTask(ProgressContext context,
                                 String taskType,
                                 String taskId,
                                 Integer total,
                                 int minIntervalMs,
                                 Runnable action) {
        context.bind(taskType, taskId, total, minIntervalMs);
        try {
            action.run();
            TaskState state = CURRENT.get();
            int processed = state == null ? 0 : state.processed;
            Integer finalTotal = state == null ? total : state.total;
            context.complete(state == null ? "" : state.phase, processed, finalTotal);
        } catch (RuntimeException e) {
            context.fail(e.getMessage());
            throw e;
        } finally {
            context.unbind();
        }
    }

    public String currentTaskId() {
        TaskState state = CURRENT.get();
        return state == null ? null : state.taskId;
    }

    public CurrentState currentState() {
        TaskState state = CURRENT.get();
        if (state == null) {
            return null;
        }
        return new CurrentState(state.phase, state.processed, state.total);
    }

    public record CurrentState(String phase, int processed, Integer total) {
    }

    private void publish(TaskState state,
                         String phase,
                         int processed,
                         Integer total,
                         ProgressStatus status,
                         String message) {
        progressReporter.report(ProgressSnapshot.builder()
                .taskId(state.taskId)
                .taskType(state.taskType)
                .phase(phase)
                .processed(processed)
                .total(total)
                .status(status)
                .message(message)
                .updatedAtMs(System.currentTimeMillis())
                .build());
    }

    private static final class TaskState {
        private final String taskType;
        private final String taskId;
        private final int minIntervalMs;
        private Integer total;
        private int processed;
        private String phase;
        private long lastSentMs;

        private TaskState(String taskType, String taskId, Integer total, int minIntervalMs) {
            this.taskType = taskType;
            this.taskId = taskId;
            this.total = total;
            this.minIntervalMs = minIntervalMs;
            this.lastSentMs = 0L;
        }
    }
}
