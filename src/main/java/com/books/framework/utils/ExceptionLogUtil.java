package com.books.framework.utils;

/**
 * 异常日志辅助：在框架/ORM 堆栈较长时，从消息行即可看到本应用首条栈帧。
 */
public final class ExceptionLogUtil {

    private static final String BIZ_PREFIX = "com.books.";

    private ExceptionLogUtil() {
    }

    /**
     * 从异常链各层栈中查找首条 {@code com.my.reconciliation} 帧，用于拼在 ERROR 消息后。
     * 无则返回空串。
     */
    public static String bizEntryHint(Throwable e) {
        if (e == null) {
            return "";
        }
        int depth = 0;
        for (Throwable t = e; t != null && depth < 32; t = t.getCause(), depth++) {
            StackTraceElement[] stack = t.getStackTrace();
            if (stack == null) {
                continue;
            }
            for (StackTraceElement ste : stack) {
                String cn = ste.getClassName();
                if (cn != null && cn.startsWith(BIZ_PREFIX)) {
                    return " [业务入口 " + ste + "]";
                }
            }
        }
        return "";
    }
}
