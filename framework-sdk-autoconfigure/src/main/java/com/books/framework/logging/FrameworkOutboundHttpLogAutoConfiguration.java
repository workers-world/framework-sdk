package com.books.framework.logging;

import com.books.framework.config.FrameworkProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(FrameworkProperties.class)
public class FrameworkOutboundHttpLogAutoConfiguration {

    public FrameworkOutboundHttpLogAutoConfiguration(FrameworkProperties properties) {
        OutboundHttpLogSettingsHolder.set(FrameworkAccessLogAutoConfiguration.toOutboundSettings(properties.getAccessLog()));
    }
}
