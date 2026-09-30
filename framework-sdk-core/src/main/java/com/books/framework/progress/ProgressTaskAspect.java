package com.books.framework.progress;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import java.lang.reflect.Method;

@Aspect
@Order(20)
public class ProgressTaskAspect {

    private final ProgressContext progressContext;
    private final ProgressTaskIdResolver progressTaskIdResolver;

    public ProgressTaskAspect(ProgressContext progressContext, ProgressTaskIdResolver progressTaskIdResolver) {
        this.progressContext = progressContext;
        this.progressTaskIdResolver = progressTaskIdResolver;
    }

    @Around("@annotation(progressTask)")
    public Object aroundProgressTask(ProceedingJoinPoint joinPoint, com.books.framework.annotation.ProgressTask progressTask)
            throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        String taskId = progressTaskIdResolver.resolve(progressTask.taskId(), method, args);
        Integer total = progressTaskIdResolver.resolveTotal(args);
        progressContext.bind(progressTask.type(), taskId, total, progressTask.minIntervalMs());
        try {
            Object result = joinPoint.proceed();
            ProgressContext.CurrentState state = progressContext.currentState();
            String phase = state == null ? "" : state.phase();
            int processed = state == null ? 0 : state.processed();
            Integer finalTotal = state == null ? total : state.total();
            if (processed <= 0 && finalTotal != null && finalTotal > 0) {
                processed = finalTotal;
            }
            progressContext.complete(phase, processed, finalTotal);
            return result;
        } catch (Throwable ex) {
            progressContext.fail(ex.getMessage());
            throw ex;
        } finally {
            progressContext.unbind();
        }
    }
}
