package com.books.framework.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** API 名称注解。 */
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
     * 框架从请求体 {@link com.books.framework.message.BaseMessage} 中取出 {@link #input()} 的第一个类型，
     * 若能解析出非空对象则按分组校验；校验在 Controller 方法体执行之前完成。
     * 默认为 {@link jakarta.validation.groups.Default} 分组。
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
