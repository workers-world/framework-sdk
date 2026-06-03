package com.books.framework.trace;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraceIdFilterMicrometerTest {

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    @AfterEach
    void tearDown() {
        TraceIdUtils.clear();
    }

    @Test
    void prefersHeaderOverMicrometerSpan() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdUtils.HEADER_NAME, "header-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> during = new AtomicReference<>();

        lenient().when(tracer.currentSpan()).thenReturn(span);
        lenient().when(span.context()).thenReturn(traceContext);
        lenient().when(traceContext.traceId()).thenReturn("span-id");

        new TraceIdFilter(new MicrometerTraceIdContributor(tracer)).doFilter(request, response, (req, res) -> during.set(TraceIdUtils.get()));

        assertEquals("header-id", during.get());
        assertNull(TraceIdUtils.get());
    }

    @Test
    void usesMicrometerSpanWhenHeaderAbsent() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> during = new AtomicReference<>();

        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("micrometer-trace-99");

        new TraceIdFilter(new MicrometerTraceIdContributor(tracer)).doFilter(request, response, (req, res) -> during.set(TraceIdUtils.get()));

        assertEquals("micrometer-trace-99", during.get());
        assertEquals("micrometer-trace-99", response.getHeader(TraceIdUtils.HEADER_NAME));
    }
}
