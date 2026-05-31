package com.books.framework.progress;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SseProgressHub implements ProgressReporter {

    private final CopyOnWriteArrayList<Subscription> subscriptions = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    public SseProgressHub() {
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 15, 15, TimeUnit.SECONDS);
    }

    public SseEmitter subscribe(String taskId) {
        SseEmitter emitter = new SseEmitter(0L);
        Subscription subscription = new Subscription(taskId, emitter);
        subscriptions.add(subscription);
        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException e) {
            subscriptions.remove(subscription);
            completeQuietly(emitter);
            log.warn("SSE 进度连接初始化失败: {}", e.getMessage());
        }
        emitter.onCompletion(() -> subscriptions.remove(subscription));
        emitter.onTimeout(() -> subscriptions.remove(subscription));
        emitter.onError(ex -> subscriptions.remove(subscription));
        return emitter;
    }

    @Override
    public void report(ProgressSnapshot snapshot) {
        if (snapshot == null || snapshot.getTaskId() == null) {
            return;
        }
        for (Subscription subscription : subscriptions) {
            if (!subscription.matches(snapshot.getTaskId())) {
                continue;
            }
            sendToEmitter(subscription.emitter, SseEmitter.event().name("progress").data(snapshot));
            if (snapshot.getStatus() == ProgressStatus.COMPLETED) {
                sendToEmitter(subscription.emitter, SseEmitter.event().name("progress-complete").data(snapshot));
            } else if (snapshot.getStatus() == ProgressStatus.FAILED) {
                sendToEmitter(subscription.emitter, SseEmitter.event().name("progress-failed").data(snapshot));
            }
        }
    }

    private void sendHeartbeat() {
        for (Subscription subscription : subscriptions) {
            sendToEmitter(subscription.emitter, SseEmitter.event().name("ping").data(System.currentTimeMillis()));
        }
    }

    private void sendToEmitter(SseEmitter emitter, SseEmitter.SseEventBuilder eventBuilder) {
        try {
            emitter.send(eventBuilder);
        } catch (Exception e) {
            subscriptions.removeIf(s -> s.emitter == emitter);
            completeQuietly(emitter);
            log.warn("SSE 进度推送失败，移除连接: {}", e.getMessage());
        }
    }

    private static void completeQuietly(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (Exception ignore) {
        }
    }

    @PreDestroy
    public void destroy() {
        heartbeatExecutor.shutdownNow();
    }

    private static final class Subscription {
        private final String taskId;
        private final SseEmitter emitter;

        private Subscription(String taskId, SseEmitter emitter) {
            this.taskId = taskId;
            this.emitter = emitter;
        }

        private boolean matches(String eventTaskId) {
            if (taskId == null || taskId.isEmpty()) {
                return true;
            }
            return taskId.equals(eventTaskId);
        }
    }
}
