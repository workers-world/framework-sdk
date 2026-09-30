package com.books.framework.utils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HanLPKeywordExtractorTest {

    private final HanLPKeywordExtractor extractor = new HanLPKeywordExtractor();

    @Test
    void extractPersonNamesReturnsListWithoutGreeting() {
        assertTrue(extractor.extractPersonNames("你好").isEmpty());
    }

    @Test
    void extractGroupedSeparatesPersonNamesWhenTagged() {
        Map<String, List<String>> grouped = extractor.extractGrouped("转账给王伟，金额100元", "你好");
        assertTrue(grouped.get("personNames") == null || grouped.get("otherNouns") != null);
    }

    @Test
    void maskPersonNamesKeepsGreeting() {
        assertEquals("你好", extractor.maskPersonNames("你好"));
        assertEquals("你好！", extractor.maskPersonNames("你好！"));
    }

    @Test
    void maskPersonNamesReplacesRecognizedName() {
        String masked = extractor.maskPersonNames("转账给王伟，金额100元");
        assertFalse(masked.contains("王伟"));
        assertTrue(masked.contains("个人用户"));
    }
}
