package com.books.framework.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonRowDiffUtilTest {

    @Test
    void diffOnlyAmountChanged() {
        String before = "{\"product_name\":\"商品A\",\"amount\":-100,\"category_id\":5}";
        String after = "{\"product_name\":\"商品A\",\"amount\":100,\"category_id\":5}";
        String changedFields = "[\"amount\"]";

        List<JsonRowDiffUtil.JsonFieldDiff> diffs = JsonRowDiffUtil.diff(before, after, changedFields, null);
        JsonRowDiffUtil.JsonFieldDiff product = find(diffs, "product_name");
        JsonRowDiffUtil.JsonFieldDiff amount = find(diffs, "amount");

        assertFalse(product.isChanged());
        assertTrue(amount.isChanged());
    }

    @Test
    void diffCamelCaseChangedFieldsWithSnakeCaseJson() {
        String before = "{\"product_name\":\"A\"}";
        String after = "{\"product_name\":\"\"}";
        String changedFields = "[\"productName\"]";

        List<JsonRowDiffUtil.JsonFieldDiff> diffs = JsonRowDiffUtil.diff(before, after, changedFields, null);
        JsonRowDiffUtil.JsonFieldDiff product = find(diffs, "product_name");

        assertTrue(product.isChanged());
        assertEquals("A", JsonRowDiffUtil.displayValue(product.getBeforeValue()));
        assertEquals("—", JsonRowDiffUtil.displayValue(product.getAfterValue()));
    }

    @Test
    void diffInsertMarksAfterFieldsChanged() {
        String after = "{\"trans_no\":\"T1\",\"amount\":-1}";
        List<JsonRowDiffUtil.JsonFieldDiff> diffs = JsonRowDiffUtil.diff(null, after, null, null);

        assertTrue(diffs.stream().allMatch(JsonRowDiffUtil.JsonFieldDiff::isChanged));
    }

    @Test
    void camelToSnakeWorks() {
        assertEquals("product_name", JsonRowDiffUtil.camelToSnake("productName"));
        assertEquals("category_id", JsonRowDiffUtil.camelToSnake("categoryId"));
    }

    private JsonRowDiffUtil.JsonFieldDiff find(List<JsonRowDiffUtil.JsonFieldDiff> diffs, String key) {
        return diffs.stream().filter(d -> key.equals(d.getFieldKey())).findFirst().orElseThrow();
    }
}
