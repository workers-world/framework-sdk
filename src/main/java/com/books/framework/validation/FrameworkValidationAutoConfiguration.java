package com.books.framework.validation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validator;

/**
 * 注册 {@link BeanValidationRunner}、{@link ApiNameBaseMessageValidationAspect}（按 {@link com.books.framework.annotation.ApiName} 自动校验）。
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(Validator.class)
@ConditionalOnBean(Validator.class)
public class FrameworkValidationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(BeanValidationRunner.class)
    public BeanValidationRunner beanValidationRunner(Validator validator) {
        return new BeanValidationRunner(validator);
    }

    @Bean
    @ConditionalOnBean(BeanValidationRunner.class)
    @ConditionalOnMissingBean(ApiNameBaseMessageValidationAspect.class)
    public ApiNameBaseMessageValidationAspect apiNameBaseMessageValidationAspect(BeanValidationRunner beanValidationRunner) {
        return new ApiNameBaseMessageValidationAspect(beanValidationRunner);
    }
}
