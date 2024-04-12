package com.zmq.aop;

import com.zmq.interceptor.*;
import com.zmq.util.ReflectionUtils;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
public class AdvisorImpl implements Advisor {

    private final AdviceType adviceType;

    private final Class<?> declaringClass;


    private final Class<?>[] parameterTypes;

    private transient Method adviceMethod;

    private MethodInterceptor advice;

    private final int declarationOrder;

    private final String methodName;

    private final AspectjPointcut pointcut;

    private AdvisorImpl(AdviceType adviceType, Class<?> declaringClass, String methodName, Class<?>[] parameterTypes, Method adviceMethod, int declarationOrder, AspectjPointcut pointcut) {
        this.adviceType = adviceType;
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.adviceMethod = adviceMethod;
        this.declarationOrder = declarationOrder;
        this.pointcut = pointcut;
        buildMethodInterceptor();

    }

    private void buildMethodInterceptor() {
        this.advice =  switch (this.adviceType){
            case Around -> new AroundAdviceInterceptor(this.adviceMethod);
            case Before -> new BeforeAdviceInterceptor(this.adviceMethod);
            case After -> new AfterAdviceInterceptor(this.adviceMethod);
            case AfterReturning -> new AfterReturningAdviceInterceptor(this.adviceMethod);
            case AfterThrowing -> new AfterThrowingAdviceInterceptor(this.adviceMethod);
            default -> null;
        };
    }

    public AdvisorImpl(Class<?> aspectAnnotationType, Method method, AspectjPointcut pointcut) {
        this(ReflectionUtils.annotationTypeMap.get(aspectAnnotationType), method.getDeclaringClass(), method.getName(), method.getParameterTypes(), method, 0, pointcut);
    }

    @Override
    public MethodInterceptor getAdvice() {
        return this.advice;
    }

    @Override
    public AspectjPointcut getPointcut() {
        return this.pointcut;
    }


    @Override
    public AdviceType getAdviceType() {
        return this.adviceType;
    }
}
