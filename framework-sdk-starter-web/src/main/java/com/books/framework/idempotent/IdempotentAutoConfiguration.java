package com.books.framework.idempotent;

import com.books.framework.config.FrameworkProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 内存幂等存储（默认）。Redis 实现见 {@code framework-sdk-starter-redis}。
 */
@AutoConfiguration
@EnableConfigurationProperties(FrameworkProperties.class)
@ConditionalOnProperty(
        prefix = "books.framework.idempotent",
        name = "store-type",
        havingValue = "memory",
        matchIfMissing = true)
public class IdempotentAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotentStorage.class)
    public DefaultIdempotentStorage defaultIdempotentStorage() {
        return new DefaultIdempotentStorage();
    }
}
