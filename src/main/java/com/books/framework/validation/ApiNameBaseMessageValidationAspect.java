package com.books.framework.validation;

import com.books.framework.annotation.ApiName;
import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;
import com.books.framework.message.BaseMessage;
import jakarta.validation.groups.Default;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * 根据 {@link ApiName#input()} 对请求中的首个入参 DTO 自动执行 Bean Validation。
 * <p>
 * 若 {@link ApiName#validationGroups()} 为空则默认使用 {@link Default} 分组。
 * 若声明了 input 但请求报文为 null 或 DTO 缺失，直接抛出 {@link AppException}(PARAM_ERROR)。
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
        Class<?>[] inputTypes = apiName.input();
        if (inputTypes == null || inputTypes.length == 0) {
            return joinPoint.proceed();
        }
        BaseMessage baseMessage = findFirstBaseMessage(joinPoint.getArgs());
        if (baseMessage == null) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "请求报文不能为空");
        }
        Class<?> dtoClass = inputTypes[0];
        Object dto = baseMessage.getDto(dtoClass);
        if (dto == null) {
            throw new AppException(ErrorCodeEnum.PARAM_ERROR, "请求体缺少 " + dtoClass.getSimpleName());
        }
        Class<?>[] groups = apiName.validationGroups();
        if (groups == null || groups.length == 0) {
            groups = new Class<?>[]{Default.class};
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
