package com.books.framework.validation;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.exception.AppException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * 对已从 {@link com.books.framework.message.BaseMessage#getDto(Class)} 解析出的 DTO 执行 Bean Validation。
 * <p>
 * 一般通过在方法上配置 {@link com.books.framework.annotation.ApiName#validationGroups()}，由
 * {@link ApiNameBaseMessageValidationAspect} 在 Controller 方法前自动调用；仅在特殊场景需要业务代码直接注入本类。
 */
public class BeanValidationRunner {

    private final Validator validator;

    public BeanValidationRunner(Validator validator) {
        this.validator = validator;
    }

    public void validate(Object dto) {
        validate(dto, (Class<?>[]) null);
    }

    @SuppressWarnings("unchecked")
    public void validate(Object dto, Class<?>... groups) {
        if (dto == null) {
            return;
        }
        Set<ConstraintViolation<Object>> violations;
        if (groups == null || groups.length == 0) {
            violations = (Set<ConstraintViolation<Object>>) (Set<?>) validator.validate(dto);
        } else {
            violations = (Set<ConstraintViolation<Object>>) (Set<?>) validator.validate(dto, groups);
        }
        if (violations.isEmpty()) {
            return;
        }
        List<ConstraintViolation<Object>> sorted = new ArrayList<>(violations);
        sorted.sort(Comparator.comparing(v -> v.getPropertyPath().toString()));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            ConstraintViolation<Object> v = sorted.get(i);
            sb.append(v.getPropertyPath()).append(": ").append(v.getMessage());
        }
        throw new AppException(ErrorCodeEnum.PARAM_ERROR, sb.toString());
    }
}
