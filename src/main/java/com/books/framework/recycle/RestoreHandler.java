package com.books.framework.recycle;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

/**
 * 恢复策略 SPI。
 * 普通表使用 {@link DefaultRestoreHandler}，树形表可在业务项目中实现此接口注册为 Bean。
 */
public interface RestoreHandler {

    boolean supports(String tableName);

    Long restoreSingle(RecycleBinEntry entry, JdbcTemplate jdbc);

    int restoreBatch(List<RecycleBinEntry> entries, JdbcTemplate jdbc);
}
