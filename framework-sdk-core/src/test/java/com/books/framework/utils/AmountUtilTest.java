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
}
