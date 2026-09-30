package com.books.framework.parse.tabular;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.io.LoadedContext;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 基于 EasyExcel 将 {@link LoadedContext} 读为带注解的行对象列表。
 *
 * <p>CSV 编码应在上游 {@link com.books.framework.io.extension.CsvResourceLoader} 中统一转为 UTF-8。
 */
@Slf4j
public final class EasyExcelRowReader {

    private EasyExcelRowReader() {
    }

    public static <T> List<T> readRows(LoadedContext ctx, Class<T> rowType) {
        ReadResult<T> result = readRowsWithErrors(ctx, rowType);
        if (!result.convertErrors().isEmpty()) {
            int previewSize = Math.min(20, result.convertErrors().size());
            log.warn("Excel转换失败共{}条，前{}条：{}", result.convertErrors().size(), previewSize,
                    result.convertErrors().subList(0, previewSize));
        }
        return result.rows();
    }

    public static <T> ReadResult<T> readRowsWithErrors(LoadedContext ctx, Class<T> rowType) {
        Objects.requireNonNull(ctx, "ctx");
        Objects.requireNonNull(rowType, "rowType");
        if (ctx.isPdfText()) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR,
                    "PDF 文本上下文请使用 PdfTextTableExtractor，不可走 EasyExcelRowReader");
        }
        ExcelTypeEnum excelType = ctx.getExcelType();
        if (excelType == null) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "LoadedContext.excelType 不能为空");
        }

        List<T> rows = new ArrayList<>();
        List<String> convertErrors = new ArrayList<>();
        try (InputStream inputStream = ctx.openStream()) {
            ExcelReaderBuilder readerBuilder = EasyExcel.read(inputStream, rowType, new AnalysisEventListener<T>() {
                @Override
                public void invoke(T data, AnalysisContext context) {
                    rows.add(data);
                }

                @Override
                public void onException(Exception exception, AnalysisContext context) throws Exception {
                    if (exception instanceof ExcelDataConvertException convertException) {
                        log.error(exception.getMessage(), exception);
                        int rowIndex = convertException.getRowIndex() + 1;
                        int columnIndex = convertException.getColumnIndex() + 1;
                        convertErrors.add(String.format("[%s行%s列]数据(%s)格式错误",
                                rowIndex, columnIndex, convertException.getCellData().getStringValue()));
                        return;
                    }
                    throw exception;
                }

                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                }
            }).excelType(excelType);
            if (excelType == ExcelTypeEnum.CSV) {
                readerBuilder.charset(StandardCharsets.UTF_8);
            }
            readerBuilder.sheet().doRead();
            return new ReadResult<>(rows, convertErrors);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(ErrorCodeEnum.FILE_READ_ERROR, "读取表格失败: " + e.getMessage(), e);
        }
    }

    public record ReadResult<T>(List<T> rows, List<String> convertErrors) {
    }
}
