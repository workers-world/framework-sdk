package com.books.framework.config;

import com.books.framework.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class FrameworkAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    FrameworkCoreAutoConfiguration.class,
                    FrameworkExceptionAutoConfiguration.class));

    @Test
    void globalExceptionHandlerIsRegistered() {
        contextRunner.run(context ->
                assertThat(context).hasSingleBean(GlobalExceptionHandler.class));
    }
}
