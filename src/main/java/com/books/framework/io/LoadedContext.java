package com.books.framework.io;

import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * 账单文件加载的中间结果。
 *
 * <p>优先使用 {@link #inputStreamSupplier} 提供可重复打开的流；若 supplier 为空，则退化为一次性流 {@link #inputStream}。
 *
 * <p>生命周期建议在编排层统一关闭（例如 {@code BillParseFlow} 使用 try-with-resources），避免“谁负责关流”分散在各处。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoadedContext implements AutoCloseable {

    private InputStream inputStream;
    private ExcelTypeEnum excelType;
    /** PDF 提取后的表格文本，需走 AnnotatedTableRowMapper 解析而非 EasyExcel */
    private boolean pdfText;
    private byte[] rawPdfBytes;

    /**
     * 可选：提供“按需打开”的流（例如从文件路径重新打开）。
     */
    private Supplier<InputStream> inputStreamSupplier;

    public InputStream openStream() {
        if (inputStream != null) {
            return inputStream;
        }
        if (inputStreamSupplier != null) {
            InputStream opened = inputStreamSupplier.get();
            this.inputStream = opened;
            return opened;
        }
        return inputStream;
    }

    public static LoadedContext simple(InputStream inputStream, ExcelTypeEnum excelType) {
        LoadedContext ctx = new LoadedContext();
        ctx.setInputStream(inputStream);
        ctx.setExcelType(excelType);
        return ctx;
    }

    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }
}
