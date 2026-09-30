package com.books.framework.id_generator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 进程内序号存储，重启后丢失；适用于单测或无 DB 场景。
 */
public class InMemoryIdSequenceStore implements IdSequenceStore {

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public long nextSeq(String prefix, String bizDate) {
        String key = prefix + ":" + bizDate;
        return counters.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }
}
