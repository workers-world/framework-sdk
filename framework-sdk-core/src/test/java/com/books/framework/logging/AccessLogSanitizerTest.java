package com.books.framework.logging;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccessLogSanitizerTest {

    @Test
    void shouldMaskConfiguredFields() {
        AccessLogSanitizer sanitizer = new AccessLogSanitizer(Set.of("sign", "password"), 2000);
        Map<String, Object> data = Map.of(
                "name", "alice",
                "sign", "secret-sign",
                "password", "secret-password"
        );

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

    @Test
    void shouldSanitizeJsonText() {
        AccessLogSanitizer sanitizer = new AccessLogSanitizer(Set.of("token"), 2000);
        String sanitized = sanitizer.sanitizeText("{\"name\":\"alice\",\"token\":\"abc\"}");

        assertThat(sanitized).contains("\"token\":\"****\"");
        assertThat(sanitized).doesNotContain("abc");
    }
}
