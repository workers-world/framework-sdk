package com.books.framework.id_generator;

import com.books.framework.utils.DateUtils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zero
 * @description todo
 * @date 2026-05-11
 */
public class IDGenerator {

    // 3位前缀
    private String prefix;

    // 当天日期
    private String curDay;

    private AtomicLong id = new AtomicLong(0);

    public IDGenerator(String prefix) {
        this.prefix = prefix;
        this.curDay = DateUtils.getCurDate();
    }

    public String genId() {
        String result;
        result = prefix + curDay + String.format("%06d", id.incrementAndGet()) ;

        return result;
    }
}
