package com.books.framework.trace;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceIdFilterTest {

    @AfterEach
    void tearDown() {
        TraceIdUtils.clear();
    }

    @Test
    void usesRequestHeaderTraceId() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(TraceIdUtils.HEADER_NAME, "  trace-from-header  ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> duringRequest = new AtomicReference<>();

        TraceIdFilter filter = new TraceIdFilter();
        filter.doFilter(request, response, chain(duringRequest));

        assertEquals("trace-from-header", duringRequest.get());
        assertEquals("trace-from-header", response.getHeader(TraceIdUtils.HEADER_NAME));
        assertNull(TraceIdUtils.get());
    }

    @Test
    void generatesTraceIdWhenHeaderMissing() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> duringRequest = new AtomicReference<>();

        new TraceIdFilter().doFilter(request, response, chain(duringRequest));

        assertNotNull(duringRequest.get());
        assertTrue(duringRequest.get().length() >= 8);
        assertEquals(duringRequest.get(), response.getHeader(TraceIdUtils.HEADER_NAME));
    }

    private static FilterChain chain(AtomicReference<String> duringRequest) {
        return (req, res) -> duringRequest.set(TraceIdUtils.get());
    }
}
