package com.books.framework.id_generator;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ID 生成器工厂，按 prefix 缓存 {@link IDGenerator} 实例。
 */
public class IDFactory {

    private final ConcurrentHashMap<String, IDGenerator> generators = new ConcurrentHashMap<>();

    public IDGenerator getIDGenerator(String prefix) {
        return generators.computeIfAbsent(prefix, IDGenerator::new);
    }
}
