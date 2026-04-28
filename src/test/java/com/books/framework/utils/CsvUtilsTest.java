package com.books.framework.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilsTest {

    @Test
    void quoteCell_nullAsEmpty() {
        assertEquals("\"\"", CsvUtils.quoteCell(null));
    }

    @Test
    void quoteCell_escapesQuotes() {
        assertEquals("\"a\"\"b\"", CsvUtils.quoteCell("a\"b"));
    }

    @Test
    void appendDataLine_oneColumn() {
        StringBuilder sb = new StringBuilder();
        CsvUtils.appendDataLine(sb, "x");
        assertEquals("\"x\"\n", sb.toString());
    }

    @Test
    void appendDataLine_multipleColumns() {
        StringBuilder sb = new StringBuilder();
        CsvUtils.appendDataLine(sb, "a", "b,c", null);
        assertEquals("\"a\",\"b,c\",\"\"\n", sb.toString());
    }
}
