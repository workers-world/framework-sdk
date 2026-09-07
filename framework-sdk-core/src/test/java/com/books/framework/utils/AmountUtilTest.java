package com.books.framework.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AmountUtilTest {

    private static void assertAmount(String expected, String raw) {
        assertTrue(new BigDecimal(expected).compareTo(AmountUtil.parseAmount(raw)) == 0,
                () -> "parseAmount(\"" + raw + "\") should equal " + expected);
    }

    @Test
    void testNormalAmount() {
        assertAmount("123.45", "123.45");
        assertAmount("0.01", "0.01");
        assertTrue(BigDecimal.ZERO.compareTo(AmountUtil.parseAmount("0")) == 0);
    }

    @Test
    void testNegativeAmount() {
        assertAmount("-50.00", "-50.00");
        assertAmount("-0.99", "-0.99");
    }

    @Test
    void testScientificNotation() {
        assertAmount("1234000000000000", "1.234E15");
        assertAmount("1234000000000000", "1.234e15");
        assertAmount("1234.56", "1.23456E3");
        assertAmount("1000000000000000000000000000000", "1E+30");
        assertAmount("0.000001", "1E-6");
    }

    @Test
    void testWithCurrencySymbol() {
        assertAmount("12.50", "¥12.50");
        assertAmount("100.00", "$100.00");
        assertAmount("8.80", "€8.80");
    }

    @Test
    void testWithWhitespace() {
        assertAmount("99.99", "  99.99  ");
        assertAmount("55.00", "\t55.00\n");
    }

    @Test
    void testWithComma() {
        assertAmount("1234.56", "1,234.56");
        assertAmount("1000000.00", "1,000,000.00");
    }

    @Test
    void testEmptyAndNull() {
        assertTrue(BigDecimal.ZERO.compareTo(AmountUtil.parseAmount(null)) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(AmountUtil.parseAmount("")) == 0);
        assertTrue(BigDecimal.ZERO.compareTo(AmountUtil.parseAmount("   ")) == 0);
    }

    @Test
    void testWithRefundSuffix() {
        assertAmount("289.00", "289.00(已全额退款)");
        assertAmount("84.60", "84.60(已退款24.90)");
        assertAmount("100.00", "100.00(已退款)");
    }

    @Test
    void testWithNegativeAndRefundSuffix() {
        assertAmount("-50.00", "-50.00(已退款10.00)");
    }

    @Test
    void testWithCurrencyCode() {
        assertAmount("-0.21", "-0.21(CN)");
        assertAmount("100.50", "100.50(US)");
        assertAmount("0.00", "0.00(CN)");
    }

    @Test
    void testInvalidAmount() {
        assertThrows(NumberFormatException.class, () -> AmountUtil.parseAmount("abc"));
        assertThrows(NumberFormatException.class, () -> AmountUtil.parseAmount("not a number"));
    }

    @Test
    void testIsValidAmount() {
        assertTrue(AmountUtil.isValidAmount("123.45"));
        assertTrue(AmountUtil.isValidAmount("1.234E15"));
        assertTrue(AmountUtil.isValidAmount("¥12.50"));

        assertFalse(AmountUtil.isValidAmount("abc"));
    }

    @Test
    void testSpaceGroupingIsRemoved() {
        // 修复前: "1 234,56" 会被清洗成 123456(静默错 100 倍)
        assertAmount("1234.56", "1 234,56");
        assertAmount("1234.56", "1 234,56");
        assertAmount("1234.56", "1,234.56");
    }

    @Test
    void testEuropeanDecimalComma() {
        assertAmount("1234.56", "1.234,56");
        assertAmount("1234.56", "1234,56");
        assertAmount("12.34", "12,34");
    }

    @Test
    void testDotGrouping() {
        assertAmount("1234567", "1.234.567");
        assertAmount("1234567.89", "1.234.567,89");
    }

    @Test
    void testAccountingNegative() {
        assertAmount("-100.00", "(100.00)");
        assertAmount("-1234.56", "(1,234.56)");
    }

    @Test
    void testSingleCommaThreeDigitsIsGrouping() {
        assertAmount("1234", "1,234");
        assertAmount("1234567", "1,234,567");
    }

    @Test
    void testAmbiguousSeparatorThrowsInsteadOfSilentCorruption() {
        // 修复前: "1.234.567.8" 被静默吞成分组(数值任人解读),现在必须显式报错
        assertThrows(NumberFormatException.class, () -> AmountUtil.parseAmount("1.234.567.8"));
        assertThrows(NumberFormatException.class, () -> AmountUtil.parseAmount("12345,67,89"));
        // 报错消息必须携带原始输入,便于定位账单行
        NumberFormatException ex = assertThrows(NumberFormatException.class,
                () -> AmountUtil.parseAmount("1.234.567.8"));
        assertTrue(ex.getMessage().contains("1.234.567.8"));
    }

    @Test
    void testGroupedAmountWithCurrencyAndSuffix() {
        assertAmount("1234.56", "¥1,234.56");
        assertAmount("1234.56", "1 234,56 EUR");
        assertAmount("-1234.56", "-1,234.56(已退款)");
    }
}
