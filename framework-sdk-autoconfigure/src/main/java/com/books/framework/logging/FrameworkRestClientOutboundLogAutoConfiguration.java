package com.books.framework.logging;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(prefix = "books.framework.access-log", name = "outbound-enabled", havingValue = "true", matchIfMissing = true)
public class FrameworkRestClientOutboundLogAutoConfiguration {

    @Bean
    public RestClientCustomizer outboundHttpLogRestClientCustomizer() {
        return builder -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            builder.requestFactory(new BufferingClientHttpRequestFactory(factory))
                    .requestInterceptor(new OutboundHttpLogInterceptor());
        };
    }
}
