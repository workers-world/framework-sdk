package com.books.framework.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.books.framework.trace.TraceIdUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutboundHttpLogInterceptorTest {

    private static final String LOGGER_NAME = "com.books.framework.logging.OutboundHttpLog";

    private final OutboundHttpLogInterceptor interceptor = new OutboundHttpLogInterceptor();
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        OutboundHttpLogSettings settings = OutboundHttpLogSettings.defaults();
        settings.setEnabled(true);
        settings.setLogRequestBody(true);
        settings.setLogResponseBody(true);
        settings.setMaskFields(Set.of("token"));
        OutboundHttpLogSettingsHolder.set(settings);

        TraceIdUtils.set("outbound-trace");

        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
        logger.setLevel(Level.DEBUG);
    }

    @AfterEach
    void tearDown() {
        TraceIdUtils.clear();
        OutboundHttpLogSettingsHolder.set(OutboundHttpLogSettings.defaults());
        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        logger.detachAppender(listAppender);
    }

    @Test
    void shouldLogSuccessfulOutboundRequest() throws IOException {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("https://api.example.com/data"));
        byte[] body = "{\"token\":\"secret\"}".getBytes(StandardCharsets.UTF_8);

        ClientHttpRequestExecution execution = (req, reqBody) ->
                new MockClientHttpResponse("{\"ok\":true}".getBytes(StandardCharsets.UTF_8), HttpStatus.OK);

        interceptor.intercept(request, body, execution);

        String infoLog = findLogContaining("outbound_http");
        assertThat(infoLog).contains("traceId=outbound-trace");
        assertThat(infoLog).contains("method=GET");
        assertThat(infoLog).contains("uri=https://api.example.com/data");
        assertThat(infoLog).contains("status=200");
        assertThat(infoLog).contains("durationMs=");
        assertThat(request.getHeaders().getFirst(TraceIdUtils.HEADER_NAME)).isEqualTo("outbound-trace");

        String requestBodyLog = findLogContaining("outbound_http_request_body");
        assertThat(requestBodyLog).contains("\"token\":\"****\"");
        assertThat(requestBodyLog).doesNotContain("secret");

        String responseBodyLog = findLogContaining("outbound_http_response_body");
        assertThat(responseBodyLog).contains("ok");
    }

    @Test
    void shouldSkipWhenDisabled() throws IOException {
        OutboundHttpLogSettings settings = OutboundHttpLogSettings.defaults();
        settings.setEnabled(false);
        OutboundHttpLogSettingsHolder.set(settings);
        listAppender.list.clear();

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("https://api.example.com/ping"));
        interceptor.intercept(request, new byte[0], (req, reqBody) ->
                new MockClientHttpResponse("ok".getBytes(StandardCharsets.UTF_8), HttpStatus.OK));

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldSkipExcludedUrl() throws IOException {
        OutboundHttpLogSettings settings = OutboundHttpLogSettings.defaults();
        settings.setEnabled(true);
        settings.setExcludeUrlPatterns(List.of("https://api.example.com/internal/**"));
        OutboundHttpLogSettingsHolder.set(settings);
        listAppender.list.clear();

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET,
                URI.create("https://api.example.com/internal/health"));
        interceptor.intercept(request, new byte[0], (req, reqBody) ->
                new MockClientHttpResponse("ok".getBytes(StandardCharsets.UTF_8), HttpStatus.OK));

        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldLogFailureAsWarn() {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create("https://api.example.com/fail"));
        ClientHttpRequestExecution execution = (req, reqBody) -> {
            throw new IOException("connection refused");
        };

        assertThatThrownBy(() -> interceptor.intercept(request, "{}".getBytes(StandardCharsets.UTF_8), execution))
                .isInstanceOf(IOException.class);

        String warnLog = findLogByLevel(Level.WARN);
        assertThat(warnLog).contains("status=FAILED");
        assertThat(warnLog).contains("error=IOException");
        assertThat(warnLog).contains("connection refused");
    }

    private String findLogContaining(String text) {
        return listAppender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains(text))
                .findFirst()
                .orElse("");
    }

    private String findLogByLevel(Level level) {
        return listAppender.list.stream()
                .filter(event -> event.getLevel() == level)
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains("outbound_http"))
                .findFirst()
                .orElse("");
    }
}
