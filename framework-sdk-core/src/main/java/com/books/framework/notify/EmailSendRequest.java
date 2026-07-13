package com.books.framework.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendRequest {

    private String subject;

    private String body;

    private String html;

    private String to;

    private String dedupKey;
}
