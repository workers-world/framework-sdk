package com.books.framework.io.extension;

import com.books.framework.io.AbstractResourceLoader;
import com.books.framework.io.LoadedContext;
import com.books.framework.parse.tabular.PdfTextTableExtractor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static com.alibaba.excel.support.ExcelTypeEnum.CSV;

@Slf4j
public class PdfResourceLoader extends AbstractResourceLoader<Object> {

    @Override
    public LoadedContext load(String path) {
        try {
            InputStream is = openResourceStream(normalizePath(path));
            byte[] raw = StreamUtils.copyToByteArray(is);
            is.close();
            String text = PdfTextTableExtractor.extract(raw);
            InputStream textStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
            return LoadedContext.simple(textStream, CSV);
        } catch (Exception e) {
            throw new RuntimeException("PDF 文件加载失败: " + path, e);
        }
    }

    @Override
    public String getName() {
        return "pdfResourceLoader";
    }
}
