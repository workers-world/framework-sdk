package com.books.framework.parse.tabular.annotation;

import java.lang.annotation.*;

/**
 * 类级注解，描述 PDF 表格文本的整体解析方式。
 * 配合字段 {@link TableColumn} 使用，无需单独实现 {@code TableParseSpec}。
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableParse {

    /** 行内分列的正则，默认两个以上连续空白 */
    String columnSeparator() default "\\s{2,}";
}
