package com.books.framework.parse.tabular;

import com.books.framework.parse.tabular.annotation.TableColumn;
import com.books.framework.parse.tabular.annotation.TableParse;
import com.books.framework.utils.AmountUtil;
import com.books.framework.utils.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 根据类型字段上的 {@link TableColumn} 将已对齐全列数组反射写入 bean（通常用于 PDF/表格文本导出的一行）。
 */
public final class AnnotatedTableRowMapper {

    private static final ConcurrentMap<Class<?>, Plan> PLAN_CACHE = new ConcurrentHashMap<>();

    private AnnotatedTableRowMapper() {
    }

    /**
     * 根据注解推导：至少应具备多少列（max(order)+1）。
     */
    public static int requiredColumnCount(Class<?> beanClass) {
        return cachedPlan(beanClass).requiredCols;
    }

    /**
     * 表头启发式：该行文本需<strong>同时包含</strong>注解列上给出的非空 {@link TableColumn#label()}
     * （与各 PDF「整行表头」常见写法一致，如分页重复表头）。
     *
     * @return true 至少参与了一个 label 匹配且无一缺失；若所有 {@code label} 均为空则返回 false。
     */
    public static boolean matchesHeaderLineByColumnLabels(Class<?> beanClass, String line) {
        if (StringUtils.isBlank(line)) {
            return false;
        }
        Plan plan = cachedPlan(beanClass);
        int usedLabels = 0;
        for (ColumnBind b : plan.binds) {
            String lbl = StringUtils.trimToEmpty(b.annotation().label());
            if (lbl.isEmpty()) {
                continue;
            }
            usedLabels++;
            if (!line.contains(lbl)) {
                return false;
            }
        }
        return usedLabels > 0;
    }

    /**
     * 将对齐后的列填入无参构造创建的实例。
     *
     * @throws ReflectiveOperationException 写入失败或类型不匹配
     * @throws IllegalArgumentException     cols 少于 {@link #requiredColumnCount(Class)}
     */
    public static <T> T mapFromAlignedColumns(Class<T> beanClass, String[] cols)
            throws ReflectiveOperationException {
        Plan plan = cachedPlan(beanClass);
        if (cols == null || cols.length < plan.requiredCols) {
            throw new IllegalArgumentException(
                    beanClass.getSimpleName() + " requires at least " + plan.requiredCols + " columns");
        }
        T instance = beanClass.getDeclaredConstructor().newInstance();
        for (ColumnBind b : plan.binds) {
            TableColumn col = b.annotation();
            Field field = b.field();
            field.setAccessible(true);
            String raw = columnTrim(cols, col.order());
            Object value;
            switch (col.valueKind()) {
                case CHINESE_AMOUNT:
                    if (field.getType() != BigDecimal.class) {
                        throw new IllegalStateException(
                                field.getName() + " has CHINESE_AMOUNT but field type must be BigDecimal");
                    }
                    value = AmountUtil.parseAmount(raw);
                    break;
                case STRING:
                default:
                    value = raw;
                    break;
            }
            field.set(instance, value);
        }
        return instance;
    }

    /**
     * 无异常包装；用于已保证列长与注解一致的场景。
     */
    public static <T> T mapFromAlignedColumnsUnchecked(Class<T> beanClass, String[] cols) {
        try {
            return mapFromAlignedColumns(beanClass, cols);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("PDF tabular mapping failed for " + beanClass.getSimpleName(), e);
        }
    }

    /**
     * 注解驱动整页解析：按 {@link TableParse} 的分列方式，逐行跳过表头后解析为 domain 对象列表。
     * 无需实现 {@code TableParseSpec}。
     */
    public static <T> List<T> parsePage(Class<T> beanClass, String pageText) {
        TableParse parseCfg = beanClass.getAnnotation(TableParse.class);
        if (parseCfg == null) {
            throw new IllegalArgumentException(beanClass.getSimpleName() + " requires @" + TableParse.class.getSimpleName());
        }
        String sep = parseCfg.columnSeparator();
        int required = requiredColumnCount(beanClass);
        List<T> rows = new ArrayList<>();
        boolean headerFound = false;
        for (String line : pageText.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            // 一旦匹配到表头行，后续才解析数据行
            if (!headerFound) {
                if (matchesHeaderLineByColumnLabels(beanClass, trimmed)) {
                    headerFound = true;
                }
                continue;
            }
            String[] cols = trimmed.split(sep, -1);
            if (cols.length >= required) {
                rows.add(mapFromAlignedColumnsUnchecked(beanClass, cols));
            }
        }
        return rows;
    }

    private static Plan cachedPlan(Class<?> beanClass) {
        return PLAN_CACHE.computeIfAbsent(beanClass, AnnotatedTableRowMapper::analyze);
    }

    private static Plan analyze(Class<?> beanClass) {
        List<ColumnBind> binds = new ArrayList<>();
        int maxIdx = -1;
        for (Field f : beanClass.getDeclaredFields()) {
            TableColumn anno = f.getAnnotation(TableColumn.class);
            if (anno == null) {
                continue;
            }
            int o = anno.order();
            if (o < 0) {
                throw new IllegalStateException(beanClass.getSimpleName() + "." + f.getName() + " order invalid");
            }
            maxIdx = Math.max(maxIdx, o);
            binds.add(new ColumnBind(f, anno));
        }
        if (binds.isEmpty()) {
            throw new IllegalStateException(beanClass.getName() + " has no @" + TableColumn.class.getSimpleName());
        }
        binds.sort(Comparator.comparingInt(b -> b.annotation().order()));
        return new Plan(binds, maxIdx + 1);
    }

    static String columnTrim(String[] cols, int idx) {
        if (cols == null || idx < 0 || idx >= cols.length) {
            return "";
        }
        return StringUtils.trimToEmpty(cols[idx]);
    }


    static final class Plan {

        final List<ColumnBind> binds;
        final int requiredCols;

        Plan(List<ColumnBind> binds, int requiredCols) {
            this.binds = binds;
            this.requiredCols = requiredCols;
        }
    }

    static final class ColumnBind {

        private final Field field;
        private final TableColumn annotation;

        ColumnBind(Field field, TableColumn annotation) {
            this.field = field;
            this.annotation = annotation;
        }

        Field field() {
            return field;
        }

        TableColumn annotation() {
            return annotation;
        }
    }
}
