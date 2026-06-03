package com.books.framework.progress;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "books.framework.progress", name = "enabled", havingValue = "true", matchIfMissing = true)
public class FrameworkProgressAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ProgressStore.class)
    @ConditionalOnProperty(prefix = "books.framework.progress", name = "store-type", havingValue = "memory", matchIfMissing = true)
    public ProgressStore inMemoryProgressStore() {
        return new InMemoryProgressStore();
    }

    @Bean
    @ConditionalOnMissingBean(SseProgressHub.class)
    public SseProgressHub sseProgressHub() {
        return new SseProgressHub();
    }

    @Bean
    @ConditionalOnMissingBean(ProgressReporter.class)
    public ProgressReporter progressReporter(ProgressStore progressStore, SseProgressHub sseProgressHub) {
        return new CompositeProgressReporter(java.util.List.of(
                new StoreProgressReporter(progressStore),
                sseProgressHub
        ));
    }

    @Bean
    @ConditionalOnMissingBean(ProgressContext.class)
    public ProgressContext progressContext(ProgressReporter progressReporter) {
        return new ProgressContext(progressReporter);
    }

    @Bean
    @ConditionalOnMissingBean(ProgressRunner.class)
    public ProgressRunner progressRunner(ProgressContext progressContext) {
        return new ProgressRunner(progressContext);
    }

    @Bean
    @ConditionalOnMissingBean(ProgressTaskIdResolver.class)
    public ProgressTaskIdResolver progressTaskIdResolver() {
        return new ProgressTaskIdResolver();
    }

    @Bean
    @ConditionalOnMissingBean(ProgressTaskAspect.class)
    public ProgressTaskAspect progressTaskAspect(ProgressContext progressContext,
                                                 ProgressTaskIdResolver progressTaskIdResolver) {
        return new ProgressTaskAspect(progressContext, progressTaskIdResolver);
    }

}
