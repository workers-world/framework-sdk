package com.books.framework.progress;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProgressContextTest {

    @Test
    void tickShouldThrottleUnlessForced() throws InterruptedException {
        CopyOnWriteArrayList<ProgressSnapshot> events = new CopyOnWriteArrayList<>();
        ProgressContext context = new ProgressContext(events::add);
        context.bind("demo", "task-1", 10, 100);
        try {
            context.tick("RUN", 1, 10);
            context.tick("RUN", 2, 10);
            context.tick("RUN", 3, 10);
            assertEquals(2, events.size());
            Thread.sleep(120);
            context.tick("RUN", 4, 10);
            assertEquals(3, events.size());
            context.tick("RUN", 10, 10);
            assertEquals(4, events.size());
            assertEquals(ProgressStatus.RUNNING, events.get(events.size() - 1).getStatus());
            assertEquals(10, events.get(events.size() - 1).getProcessed());
        } finally {
            context.unbind();
        }
    }

    @Test
    void runInTaskShouldCompleteOnSuccess() {
        CopyOnWriteArrayList<ProgressSnapshot> events = new CopyOnWriteArrayList<>();
        ProgressContext context = new ProgressContext(events::add);
        ProgressContext.runInTask(context, "demo", "task-2", 2, 0, () -> {
            context.tick("RUN", 1, 2);
            context.tick("RUN", 2, 2);
        });
        assertTrue(events.stream().anyMatch(s -> s.getStatus() == ProgressStatus.COMPLETED));
        ProgressSnapshot last = events.get(events.size() - 1);
        assertEquals(ProgressStatus.COMPLETED, last.getStatus());
        assertEquals(2, last.getProcessed());
    }
}
