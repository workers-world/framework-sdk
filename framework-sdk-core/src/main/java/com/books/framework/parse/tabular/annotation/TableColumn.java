package com.books.framework.parse.tabular.annotation;

import java.lang.annotation.*;

/**
 * 标记「tabular 一行按列对齐后」与各 PDF 导出列对应的领域字段；运行时由 {@link com.books.framework.parse.tabular.AnnotatedTableRowMapper} 写入。
 *
 * <p>{@link #order()} 为该字段在<strong>已对齐全列数组</strong> {@code cols} 中的 0-based 下标（与 PDF 表从左到右顺序一致）。
 * 该类上所有带本注解字段的 {@code max(order)+1} 即所需最少列数，无需在代码里再写死整数。
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableColumn {

    /**
     * 对齐后的列序号，从 0 开始。
     */
    int order();

    /**
     * 导出 PDF 常见表头中与该列对应的中文文案；用于可读性以及与
     * {@link com.books.framework.parse.tabular.AnnotatedTableRowMapper#matchesHeaderLineByColumnLabels(Class, String)}
     * 默认表头识别（整行内需同时出现所有非空的 label）。
     */
    String label();

    /**
     * 补充说明。
     */
    String description() default "";

    /**
     * 如何从该列原始字符串填入字段。
     */
    ColumnValueKind valueKind() default ColumnValueKind.STRING;
}
