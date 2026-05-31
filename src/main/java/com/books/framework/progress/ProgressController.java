package com.books.framework.progress;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/framework/progress")
public class ProgressController {

    private final SseProgressHub sseProgressHub;
    private final ProgressStore progressStore;

    public ProgressController(SseProgressHub sseProgressHub, ProgressStore progressStore) {
        this.sseProgressHub = sseProgressHub;
        this.progressStore = progressStore;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribeAll() {
        return sseProgressHub.subscribe(null);
    }

    @GetMapping("/subscribe/{taskId}")
    public SseEmitter subscribe(@PathVariable String taskId) {
        return sseProgressHub.subscribe(taskId);
    }

    @GetMapping("/{taskId}")
    public ProgressSnapshot get(@PathVariable String taskId) {
        return progressStore.get(taskId).orElse(null);
    }
}
