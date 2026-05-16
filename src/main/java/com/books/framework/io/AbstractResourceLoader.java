package com.books.framework.io;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author zero
 * @description todo
 * @date 2026-04-22
 */
@Slf4j
public abstract class AbstractResourceLoader<T> implements ResourceLoader {

    /**
     * 路径归一化。
     * @param path
     * @return
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

    protected InputStream openResourceStream(String path) throws Exception {
        Resource resource = new ClassPathResource(path);
        if (resource.exists()) {
            return resource.getInputStream();
        }

        return new FileInputStream(path);
    }

}
