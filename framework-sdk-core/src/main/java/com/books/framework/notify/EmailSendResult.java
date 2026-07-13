package com.books.framework.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailSendResult {

    private boolean ok;

    private boolean skipped;

    private String reason;

    private String id;

    private String error;

    private Integer status;
}
