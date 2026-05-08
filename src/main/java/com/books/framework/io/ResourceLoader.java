package com.books.framework.io;

/**
 * @author zero
 * @description 加载外部资源
 * @date 2026-04-22
 */
public interface ResourceLoader {
    /**
     * 根据路径加载外部资源。
     *
     * @param path
     */
    LoadedContext load(String path);

    String getName();
}
