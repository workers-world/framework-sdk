package com.books.framework.logging;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccessLogSanitizerTest {

    @Test
    void shouldMaskConfiguredFields() {
        AccessLogSanitizer sanitizer = new AccessLogSanitizer(Set.of("sign", "password"), 2000);
        Map<String, Object> data = new HashMap<>();
        data.put("name", "alice");
        data.put("sign", "secret-sign");
        data.put("password", "secret-password");

        String sanitized = sanitizer.sanitize(data);

        assertThat(sanitized).contains("\"name\":\"alice\"");
        assertThat(sanitized).contains("\"sign\":\"****\"");
        assertThat(sanitized).contains("\"password\":\"****\"");
        assertThat(sanitized).doesNotContain("secret-sign");
        assertThat(sanitized).doesNotContain("secret-password");
    }

    @Test
    void shouldTruncateLongBody() {
        AccessLogSanitizer sanitizer = new AccessLogSanitizer(Set.of(), 20);
        String sanitized = sanitizer.sanitize(Map.of("content", "abcdefghijklmnopqrstuvwxyz"));

        assertThat(sanitized).endsWith("...(truncated)");
        assertThat(sanitized.length()).isLessThanOrEqualTo(20 + "...(truncated)".length());
    }
}
