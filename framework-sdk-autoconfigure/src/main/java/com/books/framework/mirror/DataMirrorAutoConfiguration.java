package com.books.framework.mirror;

import com.books.framework.manager.DataMirrorManager;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class DataMirrorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataMirrorManager.class)
    public DataMirrorManager dataMirrorManager(JdbcTemplate jdbcTemplate) {
        return new DataMirrorManager(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(DataMirrorService.class)
    public DataMirrorService dataMirrorService(DataMirrorManager dataMirrorManager) {
        return new DataMirrorService(dataMirrorManager);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultDataMirrorFieldLabelProvider.class)
    public DefaultDataMirrorFieldLabelProvider defaultDataMirrorFieldLabelProvider() {
        return new DefaultDataMirrorFieldLabelProvider();
    }

    @Bean
    @ConditionalOnMissingBean(DataMirrorLabelResolver.class)
    public DataMirrorLabelResolver dataMirrorLabelResolver(
            List<DataMirrorFieldLabelProvider> providers,
            DefaultDataMirrorFieldLabelProvider defaultProvider
    ) {
        List<DataMirrorFieldLabelProvider> custom = providers.stream()
                .filter(p -> !(p instanceof DefaultDataMirrorFieldLabelProvider))
                .toList();
        return new DataMirrorLabelResolver(custom, defaultProvider);
    }

    @Bean
    @ConditionalOnMissingBean(DataMirrorQueryService.class)
    public DataMirrorQueryService dataMirrorQueryService(
            DataMirrorManager dataMirrorManager,
            DataMirrorLabelResolver labelResolver
    ) {
        return new DataMirrorQueryService(dataMirrorManager, labelResolver);
    }
}
