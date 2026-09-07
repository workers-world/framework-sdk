package com.books.framework.utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

/** 金额字符串解析与校验工具。 */
@Slf4j
public class AmountUtil {

    /**
     * 将原始金额文本解析为 {@link BigDecimal}（支持科学计数法、退款后缀、千位分组、会计负数等常见格式）。
     *
     * <p>分组分隔符识别规则:
     * <ul>
     *   <li>点与逗号同时存在:位置靠后者为小数分隔符,另一种按分组删除(如 1,234.56 / 1.234,56)</li>
     *   <li>仅逗号:重复且形如 1,234,567 按分组;单个且后随 1-2 位数字按欧式小数(1234,56 → 1234.56);
     *       单个且后随 3 位数字按美式分组(1,234 → 1234)</li>
     *   <li>仅点:重复且形如 1.234.567 按分组;单个按小数</li>
     * </ul>
     *
     * @param raw 原始金额字符串
     * @return 解析结果；空或空白返回 {@link BigDecimal#ZERO}
     * @throws NumberFormatException 无法识别的格式,消息携带原始输入
     */
    public static BigDecimal parseAmount(String raw) {
        String trimmed = raw == null ? null : raw.trim();
        if (trimmed == null || trimmed.isEmpty()) {
            return BigDecimal.ZERO;
        }
        // 先尝试直接解析（兼容科学计数法如 1.234E15），BigDecimal 原生支持
        try {
            return new BigDecimal(trimmed);
        } catch (NumberFormatException ignored) {
        }
        // 会计负数 "(100.00)"、"(1,234.56)" → 取负
        if (trimmed.length() > 2 && trimmed.charAt(0) == '(' && trimmed.charAt(trimmed.length() - 1) == ')') {
            String inner = trimmed.substring(1, trimmed.length() - 1).trim();
            return parseWithGrouping(raw, inner).negate();
        }
        // 去除退款后缀如 "289.00(已全额退款)"、"84.60(已退款24.90)"
        int parenIdx = trimmed.indexOf('(');
        if (parenIdx > 0) {
            trimmed = trimmed.substring(0, parenIdx).trim();
        }
        return parseWithGrouping(raw, trimmed);
    }

    private static BigDecimal parseWithGrouping(String raw, String trimmed) {
        // 空白(含不间断空格)一律视为千位分组
        String s = trimmed.replaceAll("[\\s\\u00A0]", "");
        // 剔除货币符号/字母等杂字符,保留数字、分隔符、负号与疑似科学计数 E
        s = s.replaceAll("[^\\d.,\\-Ee]", "");
        // 科学计数 E 的判据:后随可选符号+数字,且紧跟数字之后;其余 E(如 EUR/USD 残留)剔除
        s = s.replaceAll("[Ee](?![+-]?\\d)", "");
        s = s.replaceAll("(?<![0-9])[Ee]", "");
        boolean hasDot = s.indexOf('.') >= 0;
        boolean hasComma = s.indexOf(',') >= 0;
        if (hasDot && hasComma) {
            // 位置靠后者为小数分隔符
            if (s.lastIndexOf(',') > s.lastIndexOf('.')) {
                s = s.replace(".", "").replace(',', '.');
            } else {
                s = s.replace(",", "");
            }
        } else if (hasComma) {
            boolean repeated = s.indexOf(',') != s.lastIndexOf(',');
            if (repeated && s.matches("\\d{1,3}(,\\d{3})+([eE][+-]?\\d+)?")) {
                s = s.replace(",", "");
            } else if (!repeated && s.matches("\\d{1,3},\\d{3}([eE][+-]?\\d+)?")) {
                s = s.replace(",", "");
            } else if (!repeated && s.matches("\\d+,\\d{1,2}")) {
                s = s.replace(',', '.');
            }
            // 其余形态维持原样,由下方逗号残留检查抛出带原文的异常
        } else if (hasDot) {
            if (s.indexOf('.') != s.lastIndexOf('.') && s.matches("\\d{1,3}(\\.\\d{3})+([eE][+-]?\\d+)?")) {
                s = s.replace(".", "");
            }
        }
        if (s.indexOf(',') >= 0) {
            // 分组/小数规则未能消解逗号,宁可报错也不静默吞掉分隔符
            throw new NumberFormatException("金额解析失败(分隔符歧义): " + raw);
        }
        if (s.isEmpty()) {
            throw new NumberFormatException("金额解析失败: " + raw);
        }
        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("金额解析失败: " + raw);
        }
    }

    /**
     * 判断原始金额文本能否被 {@link #parseAmount(String)} 成功解析。
     *
     * @param raw 原始金额字符串
     * @return 可解析为 true
     */
    public static boolean isValidAmount(String raw) {
        try {
            parseAmount(raw);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
