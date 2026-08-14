package com.books.framework.io;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.InputStream;

/** 资源加载器抽象基类。 */
@Slf4j
public abstract class AbstractResourceLoader<T> implements ResourceLoader {

    /**
     * 路径归一化。
     *
     * @param path 原始资源路径
     * @return 归一化后的路径
     */
    protected String normalizePath(String path) {
        if (StringUtils.isBlank(path)) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "资源路径不能为空");
        }
        String normalized = StringUtils.trimToEmpty(path);
        boolean classpathPath = normalized.startsWith("classpath:");
        if (classpathPath) {
            normalized = normalized.substring("classpath:".length());
        }
        if (classpathPath) {
            while (normalized.startsWith("/")) {
                normalized = normalized.substring(1);
            }
        }
        return normalized;
    }

    /** 按 classpath 或文件系统路径打开资源流。 */
    protected InputStream openResourceStream(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        if (resource.exists()) {
            return resource.getInputStream();
        }

        return new FileInputStream(path);
    }

}
