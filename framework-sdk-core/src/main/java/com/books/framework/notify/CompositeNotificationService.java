package com.books.framework.notify;

import java.util.ArrayList;
import java.util.List;

/**
 * 组合通知服务 —— 将消息广播到所有注册的子实现。
 */
public class CompositeNotificationService implements NotificationService {

    private final List<NotificationService> services;

    public CompositeNotificationService(List<NotificationService> services) {
        this.services = new ArrayList<>(services != null ? services : List.of());
    }

    @Override
    public String name() {
        return "composite";
    }

    @Override
    public void notify(NotificationMessage message) {
        for (NotificationService svc : services) {
            try {
                svc.notify(message);
            } catch (Exception ignored) {
            }
        }
    }
}
