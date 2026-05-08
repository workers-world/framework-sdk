package com.books.framework.parse.tabular.annotation;

/**
 * PDF 分列文本填入 Java 字段时的解析方式。
 */
public enum ColumnValueKind {

    /**
     * 字符串：trim；空则为 ""
     */
    STRING,

    /**
     * 中国大陆账单金额：去掉千分位逗号、¥ 符号后转为 {@link java.math.BigDecimal}。
     */
    CHINESE_AMOUNT
}
