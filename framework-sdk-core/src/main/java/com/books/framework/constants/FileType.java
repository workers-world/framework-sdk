package com.books.framework.constants;

import java.util.Locale;

/**
 * 文件类型（从后缀推导）。
 */
public enum FileType {
    XLSX,
    XLS,
    CSV,
    PDF;

    public static FileType fromExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            throw new IllegalArgumentException("文件后缀不能为空");
        }
        String ext = extension.trim().toLowerCase(Locale.ROOT);
        switch (ext) {
            case "xlsx":
            case "xlsm":
                return XLSX;
            case "xls":
                return XLS;
            case "csv":
                return CSV;
            case "pdf":
                return PDF;
            default:
                throw new IllegalArgumentException("不支持的文件格式: " + extension);
        }
    }
}

