package com.books.framework.io;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * @author zero
 * @description todo
 * @date 2026-04-22
 */
@Slf4j
public abstract class AbstractResourceLoader<T> implements ResourceLoader {

    /**
     * 路径归一化。
     * @param path
     * @return
     */
    protected String normalizePath(String path) {
        if (StringUtils.isBlank(path)) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "资源路径不能为空");
        }
        String normalized = StringUtils.trimToEmpty(path);
        boolean classpathPath = normalized.startsWith("classpath:");
        if (classpathPath) {
            normalized = normalized.substring("classpath:".length());
        }
        if (classpathPath) {
            while (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
        }
        return normalized;
    }

    protected InputStream openResourceStream(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        if (resource.exists()) {
            return resource.getInputStream();
        }

        return new FileInputStream(path);
    }

    /**
     * InputStream 无文件名魔数上下文时 EasyExcel 常无法自动识别，需指定类型；
     * 后缀应来自原始文件名（见解析前写入的临时文件 extension）。
     */
    protected static ExcelTypeEnum excelTypeFromPath(String path) {
        if (path == null) {
            return ExcelTypeEnum.XLSX;
        }
        String lower = path.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".xls")) {
            return ExcelTypeEnum.XLS;
        }
        if (lower.endsWith(".csv")) {
            return ExcelTypeEnum.CSV;
        }
        if (lower.endsWith(".xlsx") || lower.endsWith(".xlsm")) {
            return ExcelTypeEnum.XLSX;
        }
        return ExcelTypeEnum.XLSX;
    }

}
