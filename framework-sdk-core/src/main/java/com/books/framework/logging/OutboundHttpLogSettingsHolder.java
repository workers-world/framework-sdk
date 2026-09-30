package com.books.framework.logging;

public final class OutboundHttpLogSettingsHolder {

    private static volatile OutboundHttpLogSettings settings = OutboundHttpLogSettings.defaults();

    private OutboundHttpLogSettingsHolder() {
    }

    public static OutboundHttpLogSettings get() {
        return settings;
    }

    public static void set(OutboundHttpLogSettings newSettings) {
        settings = newSettings != null ? newSettings : OutboundHttpLogSettings.defaults();
    }
}
