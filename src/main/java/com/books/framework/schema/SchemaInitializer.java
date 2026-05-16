package com.books.framework.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class SchemaInitializer implements ApplicationRunner, Ordered {

    private static final String SCHEMA_LOCATION = "classpath*:db/schema/*.sql";

    private final ClasspathSchemaScriptExecutor script;

    protected SchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.script = new ClasspathSchemaScriptExecutor(jdbcTemplate);
    }

    @Override
    public final void run(ApplicationArguments args) {
        List<String> scripts = discoverScripts();
        if (scripts.isEmpty()) {
            log.info("未发现Schema文件, 跳过初始化");
            return;
        }
        log.info("开始初始化数据库表结构, 发现{}个文件", scripts.size());
        for (String path : scripts) {
            script.run(path);
        }
        log.info("数据库表结构初始化完成");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private List<String> discoverScripts() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources(SCHEMA_LOCATION);
            List<String> paths = new ArrayList<>();
            for (Resource r : resources) {
                String uri = r.getURI().toString();
                int idx = uri.indexOf("db/schema/");
                if (idx >= 0) {
                    paths.add(uri.substring(idx));
                }
            }
            Collections.sort(paths);
            return paths;
        } catch (Exception e) {
            log.error("扫描Schema文件失败", e);
            return Collections.emptyList();
        }
    }
}
