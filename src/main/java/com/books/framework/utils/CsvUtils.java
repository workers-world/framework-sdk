package com.books.framework.utils;

/**
 * CSV 文本工具（RFC 4180 风格：双引号包裹字段、双引号转义为双双引号）。
 *
 * <p>适用于简单导出场景，按行向 {@link StringBuilder} 或 {@link java.io.Writer} 拼内容即可。</p>
 */
public final class CsvUtils {

    private CsvUtils() {
    }

    /**
     * 将单元格转义为 CSV 字段（带双引号）。
     *
     * @param value 原始值，null 当空串处理
     * @return 如 {@code "a""b"} 表示单元格 a"b
     */
    public static String quoteCell(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    /**
     * 追加一行数据行（末尾换行符），每列经 {@link #quoteCell(String)} 处理，列之间逗号分隔。
     *
     * @param sb  目标
     * @param cells 列值
     */
    public static void appendDataLine(StringBuilder sb, String... cells) {
        if (sb == null) {
            return;
        }
        if (cells == null || cells.length == 0) {
            sb.append('\n');
            return;
        }
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(quoteCell(cells[i]));
        }
        sb.append('\n');
    }
}
