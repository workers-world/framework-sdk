package com.books.framework.config;

import com.books.framework.idempotent.IdempotentInterceptor;
import com.books.framework.idempotent.IdempotentStorage;
import com.books.framework.progress.ProgressController;
import com.books.framework.progress.ProgressStore;
import com.books.framework.progress.SseProgressHub;
import com.books.framework.recycle.RecycleBinController;
import com.books.framework.recycle.RecycleBinService;
import com.books.framework.trace.TraceIdFilter;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import com.books.framework.idempotent.IdempotentAutoConfiguration;
import com.books.framework.redis.FrameworkRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@AutoConfigureAfter({IdempotentAutoConfiguration.class, FrameworkRedisAutoConfiguration.class})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "jakarta.servlet.Filter")
@EnableConfigurationProperties(FrameworkProperties.class)
public class FrameworkWebAutoConfiguration implements WebMvcConfigurer {

    private final IdempotentStorage idempotentStorage;

    public FrameworkWebAutoConfiguration(IdempotentStorage idempotentStorage) {
        this.idempotentStorage = idempotentStorage;
    }

    @Bean
    @ConditionalOnMissingBean(TraceIdFilter.class)
    public TraceIdFilter traceIdFilter(FrameworkProperties properties,
                                       ObjectProvider<Tracer> tracerProvider) {
        Tracer tracer = properties.getTrace().isMicrometerBridgeEnabled()
                ? tracerProvider.getIfAvailable()
                : null;
        return new TraceIdFilter(tracer);
    }

    @Bean
    @ConditionalOnBean(RecycleBinService.class)
    @ConditionalOnMissingBean(RecycleBinController.class)
    public RecycleBinController recycleBinController(RecycleBinService service) {
        return new RecycleBinController(service);
    }

    @Bean
    @ConditionalOnMissingBean(ProgressController.class)
    public ProgressController progressController(SseProgressHub sseProgressHub, ProgressStore progressStore) {
        return new ProgressController(sseProgressHub, progressStore);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new IdempotentInterceptor(idempotentStorage)).order(0);
    }
}
