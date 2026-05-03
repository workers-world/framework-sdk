package com.books.framework.idempotent;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration(proxyBeanMethods = false)
public class IdempotentAutoConfiguration implements WebMvcConfigurer {

    private final IdempotentStorage storage;

    public IdempotentAutoConfiguration(IdempotentStorage storage) {
        this.storage = storage;
    }

    @Bean
    @ConditionalOnMissingBean(IdempotentStorage.class)
    public static DefaultIdempotentStorage defaultIdempotentStorage() {
        return new DefaultIdempotentStorage();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IdempotentInterceptor(storage)).order(0);
    }
}
