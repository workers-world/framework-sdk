package com.books.framework.io;

/** 外部资源加载接口。 */
public interface ResourceLoader {
    /**
     * 根据路径加载外部资源。
     *
     * @param path 资源路径
     */
    LoadedContext load(String path);

    String getName();
}
