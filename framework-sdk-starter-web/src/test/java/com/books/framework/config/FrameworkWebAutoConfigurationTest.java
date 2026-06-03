package com.books.framework.config;

import com.books.framework.idempotent.DefaultIdempotentStorage;
import com.books.framework.idempotent.IdempotentAutoConfiguration;
import com.books.framework.idempotent.IdempotentStorage;
import com.books.framework.progress.ProgressController;
import com.books.framework.progress.InMemoryProgressStore;
import com.books.framework.progress.ProgressStore;
import com.books.framework.progress.SseProgressHub;
import com.books.framework.trace.TraceIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkWebAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    IdempotentAutoConfiguration.class,
                    FrameworkWebAutoConfiguration.class))
            .withUserConfiguration(ProgressBeansTestConfig.class);

    @Test
    void registersWebBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TraceIdFilter.class);
            assertThat(context).hasSingleBean(ProgressController.class);
            assertThat(context).hasSingleBean(FrameworkWebAutoConfiguration.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class ProgressBeansTestConfig {

        @Bean
        IdempotentStorage idempotentStorage() {
            return new DefaultIdempotentStorage();
        }

        @Bean
        ProgressStore progressStore() {
            return new InMemoryProgressStore();
        }

        @Bean
        SseProgressHub sseProgressHub() {
            return new SseProgressHub();
        }
    }
}
