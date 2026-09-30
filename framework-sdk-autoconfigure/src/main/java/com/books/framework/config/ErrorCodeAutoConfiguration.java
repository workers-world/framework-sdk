package com.books.framework.config;

import com.books.framework.spi.ErrorCodeRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfiguration
public class ErrorCodeAutoConfiguration {

    @Bean
    ErrorCodeRegistryRunner errorCodeRegistryRunner(ObjectProvider<ErrorCodeRegistrar> registrars) {
        return new ErrorCodeRegistryRunner(registrars);
    }

    static final class ErrorCodeRegistryRunner {

        ErrorCodeRegistryRunner(ObjectProvider<ErrorCodeRegistrar> registrars) {
            registrars.orderedStream().forEach(registrar -> {
                registrar.register();
                log.info("已执行错误码注册器: {}", registrar.getClass().getSimpleName());
            });
        }
    }
}
