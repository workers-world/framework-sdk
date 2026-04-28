package com.books.framework.validation;

import com.books.framework.annotation.ApiName;
import com.books.framework.message.BaseMessage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * 根据 {@link ApiName#input()}、{@link ApiName#validationGroups()} 对请求中的首个入参 DTO 自动校验（见类注释约定）。
 */
@Aspect
@Order(10)
public class ApiNameBaseMessageValidationAspect {

    private final BeanValidationRunner beanValidationRunner;

    public ApiNameBaseMessageValidationAspect(BeanValidationRunner beanValidationRunner) {
        this.beanValidationRunner = beanValidationRunner;
    }

    @Around("@annotation(apiName)")
    public Object validateBaseMessageDto(ProceedingJoinPoint joinPoint, ApiName apiName) throws Throwable {
        Class<?>[] groups = apiName.validationGroups();
        if (groups == null || groups.length == 0) {
            return joinPoint.proceed();
        }
        Class<?>[] inputTypes = apiName.input();
        if (inputTypes == null || inputTypes.length == 0) {
            return joinPoint.proceed();
        }
        BaseMessage baseMessage = findFirstBaseMessage(joinPoint.getArgs());
        if (baseMessage == null) {
            return joinPoint.proceed();
        }
        Class<?> dtoClass = inputTypes[0];
        Object dto = baseMessage.getDto(dtoClass);
        if (dto == null) {
            return joinPoint.proceed();
        }
        beanValidationRunner.validate(dto, groups);
        return joinPoint.proceed();
    }

    private static BaseMessage findFirstBaseMessage(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof BaseMessage) {
                return (BaseMessage) arg;
            }
        }
        return null;
    }
}
