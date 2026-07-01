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
}
