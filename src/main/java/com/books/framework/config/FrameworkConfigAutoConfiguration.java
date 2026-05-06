package com.books.framework.config;

import com.books.framework.utils.ConfigUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@Configuration(proxyBeanMethods = false)
public class FrameworkConfigAutoConfiguration {

    private final Environment environment;

    public FrameworkConfigAutoConfiguration(Environment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        ConfigUtils.setEnvironment(environment);
    }
}
