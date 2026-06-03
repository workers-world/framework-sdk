package com.books.framework.progress;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;

public class ProgressTaskIdResolver {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public String resolve(String taskIdExpression, Method method, Object[] args) {
        if (taskIdExpression != null && !taskIdExpression.isEmpty()) {
            MethodBasedEvaluationContext context = new MethodBasedEvaluationContext(
                    null, method, args, parameterNameDiscoverer);
            Object value = expressionParser.parseExpression(taskIdExpression).getValue(context);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return UUID.randomUUID().toString();
    }

    public Integer resolveTotal(Object[] args) {
        if (args == null) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof Collection<?> collection) {
                return collection.size();
            }
            if (arg != null && arg.getClass().isArray()) {
                return java.lang.reflect.Array.getLength(arg);
            }
        }
        return null;
    }
}
