package com.books.framework.exception;

import com.books.framework.constants.ErrorCodeEnum;
import com.books.framework.message.BaseMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 全局异常处理器（统一返回 {@link BaseMessage}）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseMessage handleAppException(AppException e, HttpServletRequest request) {
        log.error("业务异常：{}", e.getMessage(), e);
        return resolveError(request, e.getErrorCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseMessage handleMethodArgumentNotValidException(MethodArgumentNotValidException e,
                                                             HttpServletRequest request) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数校验异常：{}", errorMessage);
        return resolveError(request, ErrorCodeEnum.PARAM_ERROR.getCode(), errorMessage);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseMessage handleBindException(BindException e, HttpServletRequest request) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.error("参数绑定异常：{}", errorMessage);
        return resolveError(request, ErrorCodeEnum.PARAM_BIND_ERROR.getCode(), errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseMessage handleConstraintViolationException(ConstraintViolationException e,
                                                          HttpServletRequest request) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
        log.error("约束违反异常：{}", errorMessage);
        return resolveError(request, ErrorCodeEnum.CONSTRAINT_VIOLATION.getCode(), errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseMessage handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        log.error("参数异常：{}", e.getMessage());
        return resolveError(request, ErrorCodeEnum.PARAM_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseMessage handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        log.error("空指针异常：", e);
        return resolveError(request, ErrorCodeEnum.SYSTEM_ERROR.getCode(), "系统异常，请联系管理员");
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseMessage handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException e,
                                             HttpServletRequest request) {
        return resolveError(request, "NOT_FOUND", "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseMessage handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常：", e);
        return resolveError(request, ErrorCodeEnum.SYSTEM_ERROR.getCode(), "系统异常，请联系管理员");
    }

    private static BaseMessage resolveError(HttpServletRequest request, String code, String message) {
        Object attr = request != null ? request.getAttribute(BaseMessage.class.getName()) : null;
        if (attr instanceof BaseMessage req) {
            return req.newError(code, message);
        }
        return BaseMessage.apiError(code, message);
    }
}
