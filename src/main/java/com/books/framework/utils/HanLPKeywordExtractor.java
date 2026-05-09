package com.books.framework.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;
import java.util.stream.Collectors;

public class HanLPKeywordExtractor {

    /** 从多个文本片段中提取名词作为关键词，去重后返回逗号分隔（取前3个） */
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
        return words.stream().limit(3).collect(Collectors.joining(","));
    }

    /** 全量提取，按词性分组：人名(nr)单独一组，其他名词另一组 */
    public Map<String, List<String>> extractGrouped(String... texts) {
        Set<String> personNames = new LinkedHashSet<>();
        Set<String> otherNouns = new LinkedHashSet<>();
        for (String text : texts) {
            if (text == null || text.isBlank()) continue;
            for (Term term : HanLP.segment(text.trim())) {
                if (term.word.length() < 2) continue;
                String nature = term.nature.toString();
                if (nature.startsWith("n")) {
                    if (nature.equals("nr") || nature.equals("nrf") || nature.equals("nrj")) {
                        personNames.add(term.word);
                    } else {
                        otherNouns.add(term.word);
                    }
                }
            }
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("personNames", new ArrayList<>(personNames));
        result.put("otherNouns", new ArrayList<>(otherNouns));
        return result;
    }
}
