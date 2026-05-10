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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author zero
 * @description todo
 * @date 2026-04-30
 */
@Slf4j
public class CsvResourceLoader extends AbstractResourceLoader<Object> {
    @Override
    public LoadedContext load(String path) {
        String normalizedPath = normalizePath(path);
        ExcelTypeEnum excelType = excelTypeFromPath(normalizedPath);

        try (InputStream inputStream = openResourceStream(normalizedPath)) {
            // CSV 无统一编码：国内 Excel「另存为」多为 GBK/ANSI；直接按 UTF-8 读会中文乱码。先读入字节再检测编码。
            byte[] raw = StreamUtils.copyToByteArray(inputStream);
            CsvBytesPrepared prepared = prepareCsvBytes(raw);
            return LoadedContext.builder()
                    .inputStream(new ByteArrayInputStream(prepared.payload))
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
     * CSV 内容与检测后的编码结果（可能对 UTF-8 BOM 做了截断，避免表头首列带 BOM 字符）。
     */
    private static final class CsvBytesPrepared {
        private final byte[] payload;

        private CsvBytesPrepared(byte[] payload) {
            this.payload = payload;
        }
    }


    /**
     * 国内 Excel 「另存为 CSV」常为系统 ANSI（中文 Windows 下等价 GBK 系列）；EasyExcel CSV 默认按 UTF-8 读，
     * 不写 charset 会得到乱码。先识别 BOM / 严格 UTF-8，其余按 GB18030（覆盖 GBK）解码。
     */
    private static CsvBytesPrepared prepareCsvBytes(byte[] raw) {
        if (raw == null || raw.length == 0) {
            return new CsvBytesPrepared(new byte[0]);
        }
        // UTF-8 BOM：去掉 BOM，避免首列表头混入 U+FEFF
        if (raw.length >= 3 && raw[0] == (byte) 0xEF && raw[1] == (byte) 0xBB && raw[2] == (byte) 0xBF) {
            return new CsvBytesPrepared(Arrays.copyOfRange(raw, 3, raw.length));
        }
        if (raw.length >= 2 && raw[0] == (byte) 0xFF && raw[1] == (byte) 0xFE) {
            return new CsvBytesPrepared(Arrays.copyOfRange(raw, 2, raw.length));
        }
        if (raw.length >= 2 && raw[0] == (byte) 0xFE && raw[1] == (byte) 0xFF) {
            return new CsvBytesPrepared(Arrays.copyOfRange(raw, 2, raw.length));
        }
        if (isStrictUtf8(raw)) {
            return new CsvBytesPrepared(raw);
        }
        log.debug("CSV 非 UTF-8，使用 GB18030 解码（常见于 Excel 简体中文环境「另存为 CSV」）");
        return new CsvBytesPrepared(raw);
    }

    private static boolean isStrictUtf8(byte[] data) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            decoder.decode(ByteBuffer.wrap(data));
            return true;
        } catch (CharacterCodingException e) {
            return false;
        }
    }
}
