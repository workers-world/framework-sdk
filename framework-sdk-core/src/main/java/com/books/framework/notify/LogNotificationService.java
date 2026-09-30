package com.books.framework.notify;

import lombok.extern.slf4j.Slf4j;

/**
 * 日志通知 —— 所有消息输出到 log，适合开发调试。
 */
@Slf4j
public class LogNotificationService implements NotificationService {

    @Override
    public String name() {
        return "log";
    }

    @Override
    public void notify(NotificationMessage message) {
        if (message == null) return;
        String line = String.format("[%s] %s — %s", message.getLevel(), message.getTitle(), message.getBody());
        switch (message.getLevel()) {
            case "ERROR" -> log.error(line);
            case "WARN" -> log.warn(line);
            default -> log.info(line);
        }
    }
}
