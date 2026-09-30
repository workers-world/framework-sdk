package com.books.framework.recycle;

import com.books.framework.manager.RecycleBinManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.util.List;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class RecycleAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean(RecycleBinManager.class)
    public RecycleBinManager recycleBinManager(JdbcTemplate jdbcTemplate) {
        return new RecycleBinManager(jdbcTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(RestoreHandler.class)
    public DefaultRestoreHandler defaultRestoreHandler() {
        return new DefaultRestoreHandler();
    }

    @Bean
    @ConditionalOnMissingBean(RecycleBinService.class)
    public RecycleBinService recycleBinService(RecycleBinManager recycleBinManager,
                                               List<RestoreHandler> restoreHandlers) {
        return new RecycleBinService(recycleBinManager, restoreHandlers);
    }

    /**
     * 定时清理调度（需配置 recycle.cleanup.enabled=true 才激活）
     */
    @Configuration(proxyBeanMethods = false)
    @EnableScheduling
    @ConditionalOnProperty(name = "recycle.cleanup.enabled", havingValue = "true")
    public static class CleanupScheduler {

        private final RecycleBinService service;

        @Value("${recycle.cleanup.retention-days:90}")
        private int retentionDays;

        public CleanupScheduler(RecycleBinService service) {
            this.service = service;
        }

        @Scheduled(cron = "${recycle.cleanup.cron:0 3 3 * * ?}")
        public void cleanup() {
            service.cleanupOlderThan(retentionDays);
        }
    }
}
