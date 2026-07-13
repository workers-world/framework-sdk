package com.books.framework.notify;

import com.books.framework.config.FrameworkProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(FrameworkProperties.class)
public class NotifyWorkerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(EmailNotifyService.class)
    public EmailNotifyService emailNotifyService(FrameworkProperties properties) {
        return new NotifyWorkerEmailService(properties);
    }
}
