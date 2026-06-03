package com.books.framework.id_generator;

import com.books.framework.config.FrameworkProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(FrameworkProperties.class)
public class IdGeneratorAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnBean({DataSource.class, PlatformTransactionManager.class})
    @AutoConfigureAfter(DataSourceAutoConfiguration.class)
    @ConditionalOnProperty(
            prefix = "books.framework.id-generator",
            name = "store-type",
            havingValue = "jdbc",
            matchIfMissing = true)
    static class JdbcIdSequenceStoreConfiguration {

        @Bean
        @ConditionalOnMissingBean(IdSequenceStore.class)
        public DbIdSequenceStore dbIdSequenceStore(JdbcTemplate jdbcTemplate,
                                                   PlatformTransactionManager transactionManager) {
            return new DbIdSequenceStore(jdbcTemplate, transactionManager);
        }
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
    @ConditionalOnMissingBean(IdSequenceStore.class)
    public InMemoryIdSequenceStore fallbackInMemoryIdSequenceStore() {
        return new InMemoryIdSequenceStore();
    }

    @Bean
    @ConditionalOnMissingBean(IDFactory.class)
    public IDFactory idFactory(IdSequenceStore store) {
        return new IDFactory(store);
    }
}
