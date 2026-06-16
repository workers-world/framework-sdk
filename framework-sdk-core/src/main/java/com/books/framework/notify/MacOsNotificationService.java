package com.books.framework.notify;

import lombok.extern.slf4j.Slf4j;

/**
 * macOS 系统通知 —— 通过 osascript 弹出系统横幅。
 * 仅在 macOS 上生效；非 macOS 静默忽略。
 */
@Slf4j
public class MacOsNotificationService implements NotificationService {

    private static final String OSC_SCRIPT = "display notification \"%s\" with title \"%s\"";

    @Override
    public String name() {
        return "macos";
    }

    @Override
    public void notify(NotificationMessage message) {
        if (message == null || !isMac()) return;
        try {
            String cmd = String.format(OSC_SCRIPT, escape(message.getBody()), escape(message.getTitle()));
            new ProcessBuilder("osascript", "-e", cmd).start();
        } catch (Exception e) {
            log.warn("macOS 通知发送失败: {}", e.getMessage());
        }
    }

    private boolean isMac() {
        return System.getProperty("os.name", "").toLowerCase().contains("mac");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
