package com.books.framework.parse.tabular;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.books.framework.io.LoadedContext;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EasyExcelRowReaderTest {

    @Test
    void readCsvRowsByHeader() {
        String csv = "name,amount\nAlice,12.50\nBob,8.00\n";
        LoadedContext ctx = LoadedContext.builder()
                .inputStream(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)))
                .excelType(ExcelTypeEnum.CSV)
                .build();

        List<SampleRow> rows = EasyExcelRowReader.readRows(ctx, SampleRow.class);

        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).getName()).isEqualTo("Alice");
        assertThat(rows.get(0).getAmount()).isEqualByComparingTo("12.50");
        assertThat(rows.get(1).getName()).isEqualTo("Bob");
    }

    @Test
    void readEmptyCsvReturnsEmptyList() {
        String csv = "name,amount\n";
        LoadedContext ctx = LoadedContext.builder()
                .inputStream(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)))
                .excelType(ExcelTypeEnum.CSV)
                .build();

        List<SampleRow> rows = EasyExcelRowReader.readRows(ctx, SampleRow.class);

        assertThat(rows).isEmpty();
    }

    @Data
    public static class SampleRow {
        @ExcelProperty("name")
        private String name;

        @ExcelProperty("amount")
        private java.math.BigDecimal amount;
    }
}
