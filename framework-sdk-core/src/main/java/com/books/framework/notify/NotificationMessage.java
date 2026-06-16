package com.books.framework.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    /** 通知标题 */
    private String title;

    /** 通知正文 */
    private String body;

    /** 级别：INFO / WARN / ERROR，实现方可按级别展示不同样式 */
    @Builder.Default
    private String level = "INFO";
}
