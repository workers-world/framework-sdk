package com.books.framework.notify;

public interface EmailNotifyService {

    boolean enabled();

    EmailSendResult send(EmailSendRequest request);
}
