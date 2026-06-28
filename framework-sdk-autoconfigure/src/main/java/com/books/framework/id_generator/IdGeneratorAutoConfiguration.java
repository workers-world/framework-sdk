package com.books.framework.id_generator;

import com.books.framework.config.FrameworkProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@AutoConfiguration
@AutoConfigureAfter({
        DataSourceAutoConfiguration.class,
        JdbcTemplateAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class
})
@EnableConfigurationProperties(FrameworkProperties.class)
public class IdGeneratorAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(IdGeneratorAutoConfiguration.class);

    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnBean({DataSource.class, JdbcTemplate.class, PlatformTransactionManager.class})
    @ConditionalOnMissingBean(IdSequenceStore.class)
    @ConditionalOnProperty(
            prefix = "books.framework.id-generator",
            name = "store-type",
            havingValue = "jdbc",
            matchIfMissing = true)
    public DbIdSequenceStore dbIdSequenceStore(JdbcTemplate jdbcTemplate,
                                               PlatformTransactionManager transactionManager) {
        return new DbIdSequenceStore(jdbcTemplate, transactionManager);
    }

    @Bean
    @ConditionalOnMissingBean(IdSequenceStore.class)
    @ConditionalOnProperty(
            prefix = "books.framework.id-generator",
            name = "store-type",
            havingValue = "memory")
    public InMemoryIdSequenceStore inMemoryIdSequenceStore() {
        return new InMemoryIdSequenceStore();
    }

    @Bean
    @ConditionalOnMissingBean(IDFactory.class)
    @ConditionalOnBean(IdSequenceStore.class)
    public IDFactory idFactory(IdSequenceStore store) {
        log.info("ID 发号器已启用: store={}", store.getClass().getSimpleName());
        return new IDFactory(store);
    }
}
