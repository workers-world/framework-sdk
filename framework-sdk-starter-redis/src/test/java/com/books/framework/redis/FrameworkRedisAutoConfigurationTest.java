package com.books.framework.redis;

import com.books.framework.config.FrameworkProperties;
import com.books.framework.idempotent.IdempotentStorage;
import com.books.framework.progress.ProgressStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkRedisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FrameworkRedisAutoConfiguration.class))
            .withUserConfiguration(RedisTemplateTestConfig.class)
            .withPropertyValues(
                    "books.framework.redis.enabled=true",
                    "books.framework.idempotent.store-type=redis",
                    "books.framework.progress.store-type=redis");

    @Test
    void registersRedisBeansWhenEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RedisIdempotentStorage.class);
            assertThat(context).hasSingleBean(RedisProgressStore.class);
            assertThat(context.getBean(IdempotentStorage.class)).isInstanceOf(RedisIdempotentStorage.class);
            assertThat(context.getBean(ProgressStore.class)).isInstanceOf(RedisProgressStore.class);
        });
    }

    @Test
    void skipsBeansWhenRedisDisabled() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(FrameworkRedisAutoConfiguration.class))
                .withUserConfiguration(RedisTemplateTestConfig.class)
                .withPropertyValues("books.framework.redis.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(RedisIdempotentStorage.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class RedisTemplateTestConfig {

        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return org.mockito.Mockito.mock(StringRedisTemplate.class);
        }
    }
}
