package com.books.framework.redis;

import com.books.framework.config.FrameworkProperties;
import com.books.framework.idempotent.IdempotentStorage;
import com.books.framework.progress.ProgressStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration
@AutoConfigureBefore(name = "com.books.framework.config.FrameworkWebAutoConfiguration")
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnBean(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "books.framework.redis", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(FrameworkProperties.class)
public class FrameworkRedisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(IdempotentStorage.class)
    @ConditionalOnProperty(prefix = "books.framework.idempotent", name = "store-type", havingValue = "redis")
    public RedisIdempotentStorage redisIdempotentStorage(StringRedisTemplate redis, FrameworkProperties properties) {
        return new RedisIdempotentStorage(redis, properties);
    }

    @Bean
    @ConditionalOnMissingBean(ProgressStore.class)
    @ConditionalOnProperty(prefix = "books.framework.progress", name = "store-type", havingValue = "redis")
    public RedisProgressStore redisProgressStore(StringRedisTemplate redis,
                                                 ObjectProvider<ObjectMapper> objectMapperProvider,
                                                 FrameworkProperties properties) {
        ObjectMapper mapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);
        return new RedisProgressStore(redis, mapper, properties);
    }
}
