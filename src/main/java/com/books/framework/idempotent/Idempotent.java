package com.books.framework.idempotent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等性注解——标注在 Controller 方法上，根据请求头 X-Idempotent-Key 或 SpEL 表达式判定是否重复请求。
 * <p>重复请求返回 {@code IDEMPOTENT_DUPLICATE} 错误。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等 key 的 SpEL 表达式，为空则从请求头 X-Idempotent-Key 获取。
     */
    String key() default "";

    /**
     * 幂等记录有效时间（秒），默认 3600。
     */
    long ttlSeconds() default 3600;

    /**
     * 重复请求时的提示信息。
     */
    String message() default "请勿重复提交";
}
