package com.books.framework.logging;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

final class CachingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse delegate;
    private byte[] bodyCache;

    CachingClientHttpResponseWrapper(ClientHttpResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return delegate.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return delegate.getStatusText();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (bodyCache == null) {
            bodyCache = StreamUtils.copyToByteArray(delegate.getBody());
        }
        return new ByteArrayInputStream(bodyCache);
    }

    @Override
    public HttpHeaders getHeaders() {
        return delegate.getHeaders();
    }

    byte[] getCachedBody() throws IOException {
        if (bodyCache == null) {
            bodyCache = StreamUtils.copyToByteArray(delegate.getBody());
        }
        return bodyCache;
    }
}
