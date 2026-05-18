package com.books.framework.io.extension;

import com.alibaba.excel.support.ExcelTypeEnum;
import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.io.AbstractResourceLoader;
import com.books.framework.io.LoadedContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.Arrays;

@Slf4j
public class CsvResourceLoader extends AbstractResourceLoader<Object> {

    @Override
    public LoadedContext load(String path) {
        String normalizedPath = normalizePath(path);
        ExcelTypeEnum excelType = excelTypeFromPath(normalizedPath);

        try (InputStream inputStream = openResourceStream(normalizedPath)) {
            byte[] raw = StreamUtils.copyToByteArray(inputStream);
            byte[] utf8 = toUtf8(raw);
            return LoadedContext.builder()
                    .inputStream(new ByteArrayInputStream(utf8))
                    .excelType(excelType)
                    .build();
        } catch (Exception e) {
            throw new AppException(ErrorCodeEnum.FILE_READ_ERROR, "加载 CSV 账单失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getName() {
        return "CsvResourceLoader";
    }

    /**
     * 检测编码并转码为 UTF-8，确保下游 EasyExcel 读到统一编码。
     */
    private static byte[] toUtf8(byte[] raw) {
        if (raw == null || raw.length == 0) return new byte[0];
        // BOM 处理
        if (raw.length >= 3 && raw[0] == (byte)0xEF && raw[1] == (byte)0xBB && raw[2] == (byte)0xBF) {
            return Arrays.copyOfRange(raw, 3, raw.length); // UTF-8 BOM 去掉即可，内容已是 UTF-8
        }
        Charset source = detectCharset(raw);
        if (source == StandardCharsets.UTF_8 && raw.length < 256) {
            return raw; // 小文件且 UTF-8 通过了严格校验，无需转码
        }
        return transcode(raw, source, StandardCharsets.UTF_8);
    }

    private static Charset detectCharset(byte[] raw) {
        if (raw.length >= 2 && raw[0] == (byte)0xFF && raw[1] == (byte)0xFE) return StandardCharsets.UTF_16LE;
        if (raw.length >= 2 && raw[0] == (byte)0xFE && raw[1] == (byte)0xFF) return StandardCharsets.UTF_16BE;

        Charset utf8 = StandardCharsets.UTF_8;
        Charset gbk = Charset.forName("GB18030");
        long utf8Cjk = countCjk(decode(raw, utf8));
        long gbkCjk = countCjk(decode(raw, gbk));
        return gbkCjk > utf8Cjk ? gbk : utf8;
    }

    private static byte[] transcode(byte[] data, Charset from, Charset to) {
        try {
            return new String(data, from).getBytes(to);
        } catch (Exception e) {
            return data;
        }
    }

    private static String decode(byte[] data, Charset cs) {
        try {
            CharsetDecoder d = cs.newDecoder();
            d.onMalformedInput(CodingErrorAction.REPLACE);
            d.onUnmappableCharacter(CodingErrorAction.REPLACE);
            return d.decode(ByteBuffer.wrap(data)).toString();
        } catch (Exception e) { return ""; }
    }

    private static long countCjk(String s) {
        if (s == null || s.isEmpty()) return 0;
        return s.codePoints().filter(cp ->
            (cp >= 0x4E00 && cp <= 0x9FFF) ||
            (cp >= 0x3400 && cp <= 0x4DBF) ||
            (cp >= 0xF900 && cp <= 0xFAFF)
        ).count();
    }

    private static ExcelTypeEnum excelTypeFromPath(String path) {
        if (path == null) return ExcelTypeEnum.XLSX;
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        if (lower.endsWith(".xls")) return ExcelTypeEnum.XLS;
        if (lower.endsWith(".csv")) return ExcelTypeEnum.CSV;
        return ExcelTypeEnum.XLSX;
    }
}
