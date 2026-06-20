package com.books.framework.utils;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;
import java.util.stream.Collectors;

public class HanLPKeywordExtractor {

    private static final Set<String> PERSON_NAME_NATURES = Set.of("nr", "nrf", "nrj");

    /**
     * 从多个文本片段中提取名词作为关键词，去重后返回逗号分隔（取前3个）
     */
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

    /**
     * 全量提取，按词性分组：人名(nr)单独一组，其他名词另一组
     */
    public Map<String, List<String>> extractGrouped(String... texts) {
        Set<String> personNames = new LinkedHashSet<>();
        Set<String> otherNouns = new LinkedHashSet<>();
        for (String text : texts) {
            if (text == null || text.isBlank()) continue;

            for (Term term : HanLP.segment(text.trim())) {
                if (term.word.length() < 2) continue;

                String nature = term.nature.toString();
                if (nature.startsWith("n")) {
                    if (isPersonNameNature(nature)) {
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

    /**
     * 提取文本中 HanLP 标注的人名（nr/nrf/nrj），按出现顺序去重。
     */
    public List<String> extractPersonNames(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (Term term : HanLP.segment(text)) {
            if (term.word.length() < 2) continue;
            if (isPersonNameNature(term.nature.toString())) {
                names.add(term.word);
            }
        }
        return new ArrayList<>(names);
    }

    /**
     * 将文本中的人名替换为占位符（个人用户A/B/...），供 LLM 脱敏使用。
     */
    public String maskPersonNames(String text) {
        return maskPersonNames(text, "个人用户");
    }

    public String maskPersonNames(String text, String aliasPrefix) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String prefix = aliasPrefix == null || aliasPrefix.isEmpty() ? "个人用户" : aliasPrefix;
        Map<String, String> aliases = new LinkedHashMap<>();
        int counter = 0;
        StringBuilder sb = new StringBuilder();
        for (Term term : HanLP.segment(text)) {
            if (term.word.length() >= 2 && isPersonNameNature(term.nature.toString())) {
                String alias = aliases.get(term.word);
                if (alias == null) {
                    alias = prefix + (char) ('A' + (counter++ % 26));
                    aliases.put(term.word, alias);
                }
                sb.append(alias);
            } else {
                sb.append(term.word);
            }
        }
        return sb.toString();
    }

    private static boolean isPersonNameNature(String nature) {
        return PERSON_NAME_NATURES.contains(nature);
    }
}
