package com.books.framework.notify;

import com.books.framework.config.FrameworkProperties;
import com.books.framework.http.FrameworkHttpClient;
import com.books.framework.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NotifyWorkerEmailService implements EmailNotifyService {

    private final FrameworkProperties.NotifyWorker config;
    private final FrameworkHttpClient client;

    public NotifyWorkerEmailService(FrameworkProperties properties) {
        this.config = properties.getNotifyWorker();
        if (StringUtils.isNotBlank(config.getUrl())) {
            this.client = FrameworkHttpClient.builder()
                .baseUrl(stripTrailingSlash(config.getUrl()))
                .build();
        } else {
            this.client = null;
        }
    }

    @Override
    public boolean enabled() {
        return config.isEnabled() && client != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EmailSendResult send(EmailSendRequest request) {
        if (!enabled()) {
            return EmailSendResult.builder()
                .ok(false)
                .error("notify-worker url not configured")
                .build();
        }
        if (request == null || StringUtils.isBlank(request.getSubject())) {
            return EmailSendResult.builder()
                .ok(false)
                .error("subject 不能为空")
                .build();
        }
        if (StringUtils.isBlank(request.getBody()) && StringUtils.isBlank(request.getHtml())) {
            return EmailSendResult.builder()
                .ok(false)
                .error("body 与 html 至少提供一个")
                .build();
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("subject", request.getSubject());
        if (StringUtils.isNotBlank(request.getBody())) {
            payload.put("body", request.getBody());
        }
        if (StringUtils.isNotBlank(request.getHtml())) {
            payload.put("html", request.getHtml());
        }
        String to = StringUtils.trimToNull(request.getTo());
        if (to == null) {
            to = StringUtils.trimToNull(config.getDefaultTo());
        }
        if (to != null) {
            payload.put("to", to);
        }
        if (StringUtils.isNotBlank(request.getDedupKey())) {
            payload.put("dedupKey", request.getDedupKey());
        }

        try {
            Map<String, Object> resp = client.post("/v1/send", payload, Map.class, authHeaders());
            return parseResponse(resp);
        } catch (Exception e) {
            log.warn("notify-worker send failed: {}", e.getMessage());
            return EmailSendResult.builder()
                .ok(false)
                .error(e.getMessage())
                .build();
        }
    }

    private Map<String, String> authHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.isNotBlank(config.getToken())) {
            headers.put("Authorization", "Bearer " + config.getToken());
        }
        return headers;
    }

    private EmailSendResult parseResponse(Map<String, Object> resp) {
        if (resp == null) {
            return EmailSendResult.builder()
                .ok(false)
                .error("notify-worker 无响应")
                .build();
        }
        Object error = resp.get("error");
        if (error != null) {
            Integer status = resp.get("status") instanceof Number n ? n.intValue() : null;
            return EmailSendResult.builder()
                .ok(false)
                .error(String.valueOf(error))
                .status(status)
                .build();
        }
        boolean skipped = Boolean.TRUE.equals(resp.get("skipped"));
        String reason = resp.get("reason") != null ? String.valueOf(resp.get("reason")) : null;
        String id = resp.get("id") != null ? String.valueOf(resp.get("id")) : null;
        return EmailSendResult.builder()
            .ok(true)
            .skipped(skipped)
            .reason(reason)
            .id(id)
            .build();
    }

    private static String stripTrailingSlash(String url) {
        String trimmed = url.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
