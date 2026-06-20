package com.books.framework.logging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OutboundHttpLogSettings {

    private boolean enabled = true;
    private boolean logRequestBody = false;
    private boolean logResponseBody = false;
    private int maxBodyLength = 2000;
    private Set<String> maskFields = new HashSet<>(Set.of("sign", "password", "token", "secret"));
    private List<String> excludeUrlPatterns = new ArrayList<>();

    public static OutboundHttpLogSettings defaults() {
        return new OutboundHttpLogSettings();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isLogRequestBody() {
        return logRequestBody;
    }

    public void setLogRequestBody(boolean logRequestBody) {
        this.logRequestBody = logRequestBody;
    }

    public boolean isLogResponseBody() {
        return logResponseBody;
    }

    public void setLogResponseBody(boolean logResponseBody) {
        this.logResponseBody = logResponseBody;
    }

    public int getMaxBodyLength() {
        return maxBodyLength;
    }

    public void setMaxBodyLength(int maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    public Set<String> getMaskFields() {
        return maskFields;
    }

    public void setMaskFields(Set<String> maskFields) {
        this.maskFields = maskFields != null ? new HashSet<>(maskFields) : new HashSet<>();
    }

    public List<String> getExcludeUrlPatterns() {
        return excludeUrlPatterns;
    }

    public void setExcludeUrlPatterns(List<String> excludeUrlPatterns) {
        this.excludeUrlPatterns = excludeUrlPatterns != null ? new ArrayList<>(excludeUrlPatterns) : new ArrayList<>();
    }
}
