package com.books.framework.io.config;

import com.books.framework.io.ResourceLoader;
import com.books.framework.io.extension.CsvResourceLoader;
import com.books.framework.io.extension.ExcelResourceLoader;
import com.books.framework.io.extension.PdfResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ResourceLoaderAutoConfiguration {

    @Bean
    public CsvResourceLoader csvResourceLoader() { return new CsvResourceLoader(); }

    @Bean
    public ExcelResourceLoader excelResourceLoader() { return new ExcelResourceLoader(); }

    @Bean
    public PdfResourceLoader pdfResourceLoader() { return new PdfResourceLoader(); }
}
