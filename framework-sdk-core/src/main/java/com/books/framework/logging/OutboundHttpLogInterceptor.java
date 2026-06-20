package com.books.framework.logging;

import com.books.framework.trace.TraceIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OutboundHttpLogInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger("com.books.framework.logging.OutboundHttpLog");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        OutboundHttpLogSettings settings = OutboundHttpLogSettingsHolder.get();
        if (!settings.isEnabled()) {
            return execution.execute(request, body);
        }

        URI uri = request.getURI();
        if (isExcluded(uri, settings.getExcludeUrlPatterns())) {
            return execution.execute(request, body);
        }

        propagateTraceId(request);

        long startNanos = System.nanoTime();
        try {
            ClientHttpResponse response = execution.execute(request, body);
            boolean logResponseBody = log.isDebugEnabled() && settings.isLogResponseBody();
            CachingClientHttpResponseWrapper cachingResponse = null;
            if (logResponseBody) {
                cachingResponse = new CachingClientHttpResponseWrapper(response);
                response = cachingResponse;
            }
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
            int status = response.getStatusCode().value();
            if (status >= 400) {
                log.warn(
                        "outbound_http traceId={} method={} uri={} status={} durationMs={}",
                        safeTraceId(),
                        request.getMethod(),
                        uri,
                        status,
                        durationMs
                );
            } else {
                log.info(
                        "outbound_http traceId={} method={} uri={} status={} durationMs={}",
                        safeTraceId(),
                        request.getMethod(),
                        uri,
                        status,
                        durationMs
                );
            }
            logDebugBody(settings, body, cachingResponse);
            return response;
        } catch (IOException ex) {
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
            log.warn(
                    "outbound_http traceId={} method={} uri={} status=FAILED durationMs={} error={}: {}",
                    safeTraceId(),
                    request.getMethod(),
                    uri,
                    durationMs,
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
            logDebugRequestBody(settings, body);
            throw ex;
        }
    }

    private void propagateTraceId(HttpRequest request) {
        String traceId = TraceIdUtils.get();
        if (traceId != null && !traceId.isBlank()) {
            request.getHeaders().set(TraceIdUtils.HEADER_NAME, traceId);
        }
    }

    private boolean isExcluded(URI uri, List<String> patterns) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        String full = uri.toString();
        String path = uri.getPath() != null ? uri.getPath() : "";
        for (String pattern : patterns) {
            if (pattern == null || pattern.isBlank()) {
                continue;
            }
            if (pathMatcher.match(pattern, full) || pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private void logDebugBody(OutboundHttpLogSettings settings, byte[] requestBody,
                              CachingClientHttpResponseWrapper cachingResponse) throws IOException {
        if (!log.isDebugEnabled()) {
            return;
        }
        AccessLogSanitizer sanitizer = createSanitizer(settings);
        logDebugRequestBody(settings, requestBody, sanitizer);
        if (settings.isLogResponseBody() && cachingResponse != null) {
            String responseText = new String(cachingResponse.getCachedBody(), StandardCharsets.UTF_8);
            log.debug("outbound_http_response_body {}", sanitizer.sanitizeText(responseText));
        }
    }

    private void logDebugRequestBody(OutboundHttpLogSettings settings, byte[] requestBody) {
        if (!log.isDebugEnabled() || !settings.isLogRequestBody()) {
            return;
        }
        logDebugRequestBody(settings, requestBody, createSanitizer(settings));
    }

    private void logDebugRequestBody(OutboundHttpLogSettings settings, byte[] requestBody, AccessLogSanitizer sanitizer) {
        if (!log.isDebugEnabled() || !settings.isLogRequestBody() || requestBody == null || requestBody.length == 0) {
            return;
        }
        String requestText = new String(requestBody, StandardCharsets.UTF_8);
        log.debug("outbound_http_request_body {}", sanitizer.sanitizeText(requestText));
    }

    private AccessLogSanitizer createSanitizer(OutboundHttpLogSettings settings) {
        return new AccessLogSanitizer(settings.getMaskFields(), settings.getMaxBodyLength());
    }

    private static String safeTraceId() {
        String traceId = TraceIdUtils.get();
        return traceId != null ? traceId : "-";
    }
}
