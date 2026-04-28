package com.books.framework.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * API名称注解
 *
 * @author zero
 * @description 用于标记API接口的名称、输入输出类型等信息
 * @date 2024-12-19
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiName {
    /**
     * API名称
     *
     * @return API名称
     * @see ApiName#name()
     */
    @AliasFor("name")
    String value() default "";

    /**
     * API名称
     *
     * @return API名称
     * @see ApiName#value()
     */
    @AliasFor("value")
    String name() default "";

    /**
     * 输入参数类型
     *
     * @return 输入参数类型数组
     */
    Class[] input() default {};

    /**
     * Bean Validation 分组。
     * 非空时，框架会从请求体 {@link com.books.framework.message.BaseMessage} 中取出 {@link #input()} 的第一个类型，
     * 若能解析出非空对象则按其分组校验；校验在 Controller 方法体执行之前完成。
     * 为空表示不自动校验（兼容未声明校验的接口）。
     */
    Class<?>[] validationGroups() default {};

    /**
     * 输出参数类型
     *
     * @return 输出参数类型数组
     */
    Class[] output() default {};

    /**
     * API描述
     *
     * @return API描述
     */
    String description() default "";

    /**
     * API版本
     *
     * @return API版本
     */
    String version() default "1.0";
}
