package com.books.framework.parse.tabular;

import com.books.framework.parse.tabular.annotation.TableColumn;
import jakarta.annotation.Nullable;

/**
 * 结构化 PDF（按文本行）解析约定：先用表头特征识别跳过表头行，再对数据行解析为业务领域对象。
 *
 * @param <T> 单条账单/行对应的 domain 类型
 */
public interface TableParseSpec<T> {

    /**
     * 与本解析器输出对应的 domain 类型；须带 {@link TableColumn} 方可用默认表头推断。
     */
    Class<T> rowType();

    /**
     * 是否为本格式的表头行（含 PDF 分页重复表头）。默认依据 {@link #rowType()} 上各列非空 {@code label}
     * 是否<strong>都在</strong>该行出现；不适用时可覆盖。
     *
     * @param line 已 trim，非空
     */
    default boolean isHeaderLine(String line) {
        return AnnotatedTableRowMapper.matchesHeaderLineByColumnLabels(rowType(), line);
    }

    /**
     * 将一行解析为 domain；若非本表数据行或无法解析返回 {@code null}。
     */
    @Nullable
    T parseDataLine(String line);
}
