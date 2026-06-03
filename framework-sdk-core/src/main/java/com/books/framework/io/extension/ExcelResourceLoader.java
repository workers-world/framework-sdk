package com.books.framework.io.extension;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.io.AbstractResourceLoader;
import com.books.framework.io.LoadedContext;

/**
 * @author zero
 * @description todo
 * @date 2026-04-30
 */
public class ExcelResourceLoader extends AbstractResourceLoader<Object> {
    /**
     * 交由调用方在编排层统一关闭（通常为 {@code BillParseFlow} 的 try-with-resources）；
     * 此处<strong>不可用</strong> {@code try (InputStream ...)}——否则返回时流会先被关闭，
     * EasyExcel 读临时文件时出现 {@code Stream Closed} / {@code Can not create temporary file!}。
     */
    @Override
    public LoadedContext load(String path) {
        String normalizedPath = normalizePath(path);
        ExcelTypeEnum excelType = excelTypeFromPath(normalizedPath);

        try {
            return LoadedContext.builder()
                    .excelType(excelType)
                    .inputStreamSupplier(() -> {
                        try {
                            return openResourceStream(normalizedPath);
                        } catch (Exception e) {
                            throw new AppException(ErrorCodeEnum.FILE_READ_ERROR, "加载Excel账单失败: " + e.getMessage(), e);
                        }
                    })
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCodeEnum.FILE_READ_ERROR, "加载Excel账单失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "ExcelResourceLoader";
    }

    private static ExcelTypeEnum excelTypeFromPath(String path) {
        if (path == null) return ExcelTypeEnum.XLSX;
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".xls")) return ExcelTypeEnum.XLS;
        if (lower.endsWith(".xlsx") || lower.endsWith(".xlsm")) return ExcelTypeEnum.XLSX;
        return ExcelTypeEnum.XLSX;
    }
}
