package com.books.framework.notify;

/**
 * 通知服务 SPI —— 业务方注入具体实现（日志、系统弹窗、邮件、企微等）。
 * 默认使用 {@link CompositeNotificationService} 聚合多个实现，通过配置控制开关。
 */
public interface NotificationService {

    /**
     * @return 实现名称，便于日志区分
     */
    String name();

    /**
     * 发送通知。实现应自身处理异常，不应向上抛出打断业务。
     */
    void notify(NotificationMessage message);
}
