package com.books.framework.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 异常日志辅助：在框架/ORM 堆栈较长时，从消息行即可看到本应用首条栈帧。
 *
 * <p>识别规则(二选一):
 * <ul>
 *   <li>配置系统属性 {@code books.framework.biz-prefixes}(逗号分隔的包前缀)时,
 *       返回首个命中前缀的帧——适配包名确定的场景;</li>
 *   <li>未配置时采用排除法:跳过 JDK/框架/本 SDK 等已知非业务帧,返回第一条其余帧。
 *       SDK 无法预知应用包名,排除法是合理的默认(历史版本硬编码 {@code com.books.},
 *       永远只会命中 SDK 自身帧)。</li>
 * </ul>
 */
public final class ExceptionLogUtil {

    /** 业务包前缀系统属性(逗号分隔)。 */
    public static final String BIZ_PREFIXES_PROPERTY = "books.framework.biz-prefixes";

    /** 排除法默认跳过的非业务包前缀。 */
    private static final List<String> DEFAULT_SKIP_PREFIXES = List.of(
            "java.", "javax.", "jakarta.", "jdk.", "sun.", "com.sun.",
            "com.books.",
            "org.springframework.", "org.apache.", "org.hibernate.", "org.mybatis.",
            "org.slf4j.", "org.jboss.", "org.glassfish.",
            "io.netty.", "reactor.", "kotlin.", "ch.qos.logback.");

    private ExceptionLogUtil() {
    }

    /**
     * 从异常链各层栈中查找首条业务帧，用于拼在 ERROR 消息后。
     * 无则返回空串。
     */
    public static String bizEntryHint(Throwable e) {
        if (e == null) {
            return "";
        }
        List<String> prefixes = configuredPrefixes();
        int depth = 0;
        for (Throwable t = e; t != null && depth < 32; t = t.getCause(), depth++) {
            StackTraceElement[] stack = t.getStackTrace();
            if (stack == null) {
                continue;
            }
            for (StackTraceElement ste : stack) {
                String cn = ste.getClassName();
                if (prefixes != null) {
                    if (matchesAny(cn, prefixes)) {
                        return " [业务入口 " + ste + "]";
                    }
                } else if (!matchesAny(cn, DEFAULT_SKIP_PREFIXES)) {
                    return " [业务入口 " + ste + "]";
                }
            }
        }
        return "";
    }

    private static List<String> configuredPrefixes() {
        String raw = System.getProperty(BIZ_PREFIXES_PROPERTY);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        List<String> prefixes = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (!part.isBlank()) {
                prefixes.add(part.trim());
            }
        }
        return prefixes.isEmpty() ? null : prefixes;
    }

    private static boolean matchesAny(String className, List<String> prefixes) {
        for (String prefix : prefixes) {
            if (className.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
