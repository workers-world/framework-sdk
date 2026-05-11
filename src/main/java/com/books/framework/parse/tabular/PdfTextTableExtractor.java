package com.books.framework.parse.tabular;

import com.books.framework.utils.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

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

    /**
     * tabula 坐标定位提取：输出每行的列文本数组，自动处理表头跳过、折行合并。
     * 对银行账单等标准表格 PDF 效果好于纯文本解析。
     */
    public static List<List<String>> extractTable(byte[] raw) {
        try (PDDocument doc = PDDocument.load(raw)) {
            // 先尝试 Spreadsheet（有线框表格），不行再用 Basic（纯文本坐标定位）
            List<List<String>> rows = extractWithAlgo(doc, new SpreadsheetExtractionAlgorithm());
            if (!rows.isEmpty()) return rows;
            return extractWithAlgo(doc, new BasicExtractionAlgorithm());
        } catch (IOException e) {
            throw new RuntimeException("tabula PDF 表格提取失败", e);
        }
    }

    private static List<List<String>> extractWithAlgo(PDDocument doc, Object algo) {
        List<List<String>> rows = new ArrayList<>();
        try {
            PageIterator pages = new ObjectExtractor(doc).extract();
            while (pages.hasNext()) {
                Page page = pages.next();
                List<Table> tables;
                if (algo instanceof SpreadsheetExtractionAlgorithm) {
                    tables = ((SpreadsheetExtractionAlgorithm) algo).extract(page);
                } else {
                    tables = ((BasicExtractionAlgorithm) algo).extract(page);
                }
                for (Table table : tables) {
                    for (List<RectangularTextContainer> row : table.getRows()) {
                        List<String> cells = new ArrayList<>(row.size());
                        for (RectangularTextContainer cell : row) {
                            cells.add(StringUtils.trimToEmpty(cell.getText()));
                        }
                        if (cells.stream().anyMatch(s -> !s.isEmpty())) {
                            rows.add(cells);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("tabula PDF 表格提取失败", e);
        }
        return rows;
    }

    /**
     * tabula 提取后映射到 domain：表头跳过 + 按 colCount 分列 → 注解驱动装配。
     */
    public static <T> List<T> extractTableAndMap(byte[] raw, Class<T> beanClass) {
        List<List<String>> rows = extractTable(raw);
        List<T> result = new ArrayList<>();
        int required = AnnotatedTableRowMapper.requiredColumnCount(beanClass);
        boolean headerFound = false;
        for (List<String> row : rows) {
            String line = String.join(" ", row);
            if (!headerFound) {
                if (AnnotatedTableRowMapper.matchesHeaderLineByColumnLabels(beanClass, line)) {
                    headerFound = true;
                }
                continue;
            }
            String[] cols = new String[Math.max(row.size(), required)];
            for (int i = 0; i < row.size(); i++) cols[i] = row.get(i);
            for (int i = row.size(); i < required; i++) cols[i] = "";
            result.add(AnnotatedTableRowMapper.mapFromAlignedColumnsUnchecked(beanClass, cols));
        }
        return result;
    }

    public static String extract(byte[] raw) {
        try (PDDocument doc = PDDocument.load(raw)) {
            return newPdfTextStripperSorted().getText(doc);
        } catch (IOException e) {
            throw new RuntimeException("PDF 文本提取失败", e);
        }
    }

    /** 不排序提取：保持 PDF 自然文本流顺序，适用于 CMB 等普通表格 */
    public static String extractUnsorted(byte[] raw) {
        try (PDDocument doc = PDDocument.load(raw)) {
            return new PDFTextStripper().getText(doc);
        } catch (IOException e) {
            throw new RuntimeException("PDF 文本提取失败", e);
        }
    }

    public static String extract(InputStream is) {
        try (PDDocument doc = PDDocument.load(is)) {
            return newPdfTextStripperSorted().getText(doc);
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
