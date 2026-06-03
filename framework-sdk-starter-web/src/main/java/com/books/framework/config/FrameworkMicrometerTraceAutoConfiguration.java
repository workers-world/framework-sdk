package com.books.framework.config;

import com.books.framework.trace.TraceIdContributor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;

/**
 * Micrometer 为 optional 依赖：本类字节码中不引用 {@code Tracer}，避免消费方未引入 tracing 时类型解析失败。
 */
@AutoConfiguration
@AutoConfigureBefore(name = "com.books.framework.config.FrameworkWebAutoConfiguration")
@ConditionalOnClass(name = "io.micrometer.tracing.Tracer")
@ConditionalOnProperty(prefix = "books.framework.trace", name = "micrometer-bridge-enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(FrameworkProperties.class)
public class FrameworkMicrometerTraceAutoConfiguration {

    private static final String TRACER_CLASS_NAME = "io.micrometer.tracing.Tracer";
    private static final String CONTRIBUTOR_CLASS_NAME = "com.books.framework.trace.MicrometerTraceIdContributor";

    @Bean
    @ConditionalOnMissingBean(TraceIdContributor.class)
    TraceIdContributor micrometerTraceIdContributor(BeanFactory beanFactory) {
        ClassLoader classLoader = beanFactory.getClass().getClassLoader();
        Class<?> tracerClass = ClassUtils.resolveClassName(TRACER_CLASS_NAME, classLoader);
        Object tracer = beanFactory.getBeanProvider(tracerClass).getIfAvailable();
        Class<?> contributorClass = ClassUtils.resolveClassName(CONTRIBUTOR_CLASS_NAME, classLoader);
        try {
            Constructor<?> constructor = contributorClass.getConstructor(tracerClass);
            return (TraceIdContributor) constructor.newInstance(tracer);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to create MicrometerTraceIdContributor", ex);
        }
    }
}
