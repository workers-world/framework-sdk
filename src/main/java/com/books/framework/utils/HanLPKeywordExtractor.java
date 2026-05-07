package com.books.framework.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;
import java.util.stream.Collectors;

public class HanLPKeywordExtractor {

    /** 从多个文本片段中提取名词作为关键词，去重后返回逗号分隔 */
    public String extract(String... texts) {
        Set<String> words = new LinkedHashSet<>();
        for (String text : texts) {
            if (text == null || text.isBlank()) continue;
            for (Term term : HanLP.segment(text.trim())) {
                if (term.word.length() < 2) continue;
                if (term.nature.toString().startsWith("n")) {
                    words.add(term.word);
                }
            }
        }
        // 取前3个
        return words.stream().limit(3).collect(Collectors.joining(","));
    }
}
