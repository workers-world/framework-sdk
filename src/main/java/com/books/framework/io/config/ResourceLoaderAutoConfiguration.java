package com.books.framework.io.config;

import com.books.framework.io.ResourceLoader;
import com.books.framework.io.extension.CsvResourceLoader;
import com.books.framework.io.extension.ExcelResourceLoader;
import com.books.framework.io.extension.PdfResourceLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.alibaba.excel.EasyExcel")
public class ResourceLoaderAutoConfiguration {

    @Bean
    @ConditionalOnClass(name = "com.alibaba.excel.EasyExcel")
    public CsvResourceLoader csvResourceLoader() { return new CsvResourceLoader(); }

    @Bean
    @ConditionalOnClass(name = "org.apache.poi.ss.usermodel.Workbook")
    public ExcelResourceLoader excelResourceLoader() { return new ExcelResourceLoader(); }

    @Bean
    @ConditionalOnClass(name = "technology.tabula.Table")
    public PdfResourceLoader pdfResourceLoader() { return new PdfResourceLoader(); }
}
