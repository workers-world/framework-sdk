package com.books.framework.id_generator;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ID 生成器工厂，按 prefix 缓存 {@link IDGenerator} 实例。
 */
public class IDFactory {

    private final IdSequenceStore store;
    private final ConcurrentHashMap<String, IDGenerator> generators = new ConcurrentHashMap<>();

    public IDFactory() {
        this(new InMemoryIdSequenceStore());
    }

    public IDFactory(IdSequenceStore store) {
        this.store = store;
    }

    public IDGenerator getIDGenerator(String prefix) {
        return generators.computeIfAbsent(prefix, p -> new IDGenerator(p, store));
    }
}
