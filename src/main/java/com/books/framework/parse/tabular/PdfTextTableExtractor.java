package com.books.framework.parse.tabular;

import com.books.framework.utils.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用「PDF 抽出文本 → 按行拆分 → 跳过表头 → 交由 {@link TableParseSpec} 抽取 domain」。
 */
public final class PdfTextTableExtractor {

    private PdfTextTableExtractor() {
    }

    public static String extract(byte[] raw) {
        try (PDDocument doc = PDDocument.load(raw)) {
            return new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            throw new RuntimeException("PDF 文本提取失败", e);
        }
    }

    public static String extract(InputStream is) {
        try (PDDocument doc = PDDocument.load(is)) {
            return new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            throw new RuntimeException("PDF 文本提取失败", e);
        }
    }

    /**
     * 与现有账单解析保持一致：排序后抽取，表格列对齐更稳定。
     */
    public static String extractPlainTextSorted(PDDocument document) throws IOException {
        PDFTextStripper stripper = newPdfTextStripperSorted();
        return stripper.getText(document);
    }

    public static PDFTextStripper newPdfTextStripperSorted() throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        return stripper;
    }

    /**
     * @param pdfFile PDF 路径
     * @param spec    首行（表头）识别 + domain 抽取
     * @throws IOException PDDocument.load / 文本抽取失败
     */
    public static <T> List<T> extractFromPdfFile(File pdfFile, TableParseSpec<T> spec) throws IOException {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            String text = extractPlainTextSorted(document);
            return extractRows(text, spec);
        }
    }

    /**
     * @param fullText PDF 抽出后的全文（可独立单测或与来源解耦）
     */
    public static <T> List<T> extractRows(String fullText, TableParseSpec<T> spec) {
        List<T> result = new ArrayList<>();
        if (StringUtils.isBlank(fullText)) {
            return result;
        }
        String[] lines = fullText.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = StringUtils.trimToEmpty(rawLine);
            if (line.isEmpty()) {
                continue;
            }
            if (spec.isHeaderLine(line)) {
                continue;
            }
            T row = spec.parseDataLine(line);
            if (row != null) {
                result.add(row);
            }
        }
        return result;
    }

    /**
     * 单行日志预览，裁剪空白与长度
     */
    public static String previewForLog(String text) {
        if (text == null) {
            return "";
        }
        String oneLine = text.replace('\r', ' ').replace('\n', ' ').trim();
        int max = 200;
        return oneLine.length() <= max ? oneLine : oneLine.substring(0, max) + "...";
    }
}
