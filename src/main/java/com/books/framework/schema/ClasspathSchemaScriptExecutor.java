package com.books.framework.schema;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
public class ClasspathSchemaScriptExecutor {

    private final JdbcTemplate jdbcTemplate;

    public ClasspathSchemaScriptExecutor(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void run(String classpath) {
        String sql = readClasspathUtf8(classpath);
        if (sql == null || sql.isBlank()) {
            return;
        }
        splitAndExecute(sql);
    }

    private void splitAndExecute(String sql) {
        sql = sql.replaceAll("/\\*.*?\\*/", "");
        StringBuilder stmt = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                continue;
            }
            stmt.append(line).append("\n");
            if (trimmed.endsWith(";")) {
                String single = stmt.toString().trim();
                if (single.endsWith(";")) {
                    single = single.substring(0, single.length() - 1);
                }
                if (!single.isBlank()) {
                    try {
                        jdbcTemplate.execute(single);
                    } catch (Exception e) {
                        log.warn("SQL执行警告(已忽略): {}", e.getMessage());
                    }
                }
                stmt.setLength(0);
            }
        }
    }

    private String readClasspathUtf8(String path) {
        try {
            Resource resource = new DefaultResourceLoader().getResource("classpath:" + path);
            if (!resource.exists()) {
                log.warn("Schema文件不存在: {}", path);
                return null;
            }
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取Schema文件失败: {}", path, e);
            return null;
        }
    }
}
