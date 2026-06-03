package com.books.framework.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(FrameworkProperties.class)
@Import({
        FrameworkConfigAutoConfiguration.class,
        FrameworkMessageAutoConfiguration.class,
        FrameworkExceptionAutoConfiguration.class
})
public class FrameworkCoreAutoConfiguration {
}
