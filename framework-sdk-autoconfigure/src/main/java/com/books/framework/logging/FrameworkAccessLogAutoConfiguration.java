package com.books.framework.logging;

import com.books.framework.config.FrameworkProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "books.framework.access-log", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FrameworkAccessLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AccessLogSanitizer.class)
    public AccessLogSanitizer accessLogSanitizer(FrameworkProperties properties) {
        FrameworkProperties.AccessLog accessLog = properties.getAccessLog();
        return new AccessLogSanitizer(new HashSet<>(accessLog.getMaskFields()), accessLog.getMaxBodyLength());
    }

    @Bean
    @ConditionalOnMissingBean(ApiAccessLogAspect.class)
    public ApiAccessLogAspect apiAccessLogAspect(FrameworkProperties properties, AccessLogSanitizer sanitizer) {
        return new ApiAccessLogAspect(properties.getAccessLog(), sanitizer);
    }
}
