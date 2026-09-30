package com.books.framework.id_generator;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * JDBC 持久化序号存储，重启后从 DB 续号。
 */
public class DbIdSequenceStore implements IdSequenceStore {

    private static final Logger log = LoggerFactory.getLogger(DbIdSequenceStore.class);

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    public DbIdSequenceStore(JdbcTemplate jdbc, PlatformTransactionManager transactionManager) {
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactionManager);
    }

    @PostConstruct
    public void initTable() {
        try {
            jdbc.execute("CREATE TABLE IF NOT EXISTS framework_id_sequence (" +
                    "prefix VARCHAR(16) NOT NULL, " +
                    "biz_date CHAR(8) NOT NULL, " +
                    "cur_seq BIGINT NOT NULL DEFAULT 0, " +
                    "max_seq BIGINT NOT NULL DEFAULT 999999, " +
                    "PRIMARY KEY (prefix, biz_date)" +
                    ")");
            log.info("framework_id_sequence 表初始化完成");
        } catch (Exception e) {
            log.warn("framework_id_sequence 表初始化失败(数据库未就绪,稍后重试): {}", e.getMessage());
        }
    }

    @Override
    public long nextSeq(String prefix, String bizDate) {
        Long seq = tx.execute(status -> {
            jdbc.update(
                    "INSERT INTO framework_id_sequence (prefix, biz_date, cur_seq) VALUES (?, ?, 1) " +
                            "ON DUPLICATE KEY UPDATE cur_seq = LAST_INSERT_ID(cur_seq + 1)",
                    prefix, bizDate);
            Long value = jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            if (value == null) {
                throw new IllegalStateException("发号失败: prefix=" + prefix + ", bizDate=" + bizDate);
            }
            if (value > IDGenerator.MAX_SEQ) {
                throw new IllegalStateException(
                        "ID 序号溢出: prefix=" + prefix + ", bizDate=" + bizDate + ", seq=" + value);
            }
            return value;
        });
        if (seq == null) {
            throw new IllegalStateException("发号失败: prefix=" + prefix + ", bizDate=" + bizDate);
        }
        return seq;
    }
}
