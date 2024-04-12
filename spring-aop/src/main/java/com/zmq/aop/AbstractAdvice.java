package com.zmq.aop;

import com.zmq.exception.AopConfigException;
import com.zmq.util.ReflectionUtils;
import jakarta.annotation.Nullable;
import lombok.Data;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author zmq
 */
@Data
public abstract class AbstractAdvice {

    private final AdviceType adviceType;
    /**
     * 所属切面类
     */
    private final Class<?> declaringClass;

    /**
     * 方法名
     */
    private final String methodName;

    /**
     * 方法所有参数的类型
     */
    private final Class<?>[] parameterTypes;

    /**
     * 方法实例对象
     */
    protected transient Method adviceMethod;

    /**
     * 所属切面的名称
     */
    private String aspectName = "";

    /**
     * 该advice在切面里的排序
     */
    private int declarationOrder;

    /**
     * 方法的参数名称，可以为null
     */
    @Nullable
    private String[] argumentNames;

    private Object aspectInstance;

    /**
     * 如果该 advice 是 afterThrow advice 且绑定了抛出的异常，则值不能为null
     */
    @Nullable
    private String throwingName;

    /**
     * 如果该 advice 是 afterReturning advice 且绑定了返回值，则值不能为null
     */
    @Nullable
    private String returningName;

    private Class<?> discoveredReturningType = Object.class;

    private Class<?> discoveredThrowingType = Object.class;

    /**
     * Index for thisJoinPoint argument (currently only
     * supported at index 0 if present at all).
     */
    private int joinPointArgumentIndex = -1;

    /**
     * Index for thisJoinPointStaticPart argument (currently only
     * supported at index 0 if present at all).
     */
    private int joinPointStaticPartArgumentIndex = -1;

    @Nullable
    private Map<String, Integer> argumentBindings;

    private boolean argumentsIntrospected = false;

    @Nullable
    private Type discoveredReturningGenericType;

    public AbstractAdvice(Method adviceMethod) {
        this.declaringClass = adviceMethod.getDeclaringClass();
        this.methodName = adviceMethod.getName();
        this.parameterTypes = adviceMethod.getParameterTypes();
        this.adviceMethod = adviceMethod;
        this.adviceType = ReflectionUtils.findAspectAnnotationTypeOnMethod(adviceMethod);
        try {
            this.aspectInstance = this.declaringClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AopConfigException(e);
        }
    }

    protected Object invokeAdviceMethod(MethodInvocation methodInvocation, @Nullable Object returnValue, Throwable ex) throws InvocationTargetException, IllegalAccessException {
        MethodInvocationProceedingJoinPoint proceedingJoinPoint = new MethodInvocationProceedingJoinPoint((ProxyMethodInvocation) methodInvocation);
        if (adviceType == AdviceType.Around) {
            return this.adviceMethod.invoke(this.aspectInstance, proceedingJoinPoint);
        } else {
            if (this.parameterTypes.length==0){
                return this.adviceMethod.invoke(this.aspectInstance);
            }
            if (adviceType == AdviceType.Before || adviceType == AdviceType.After) {
                if (this.parameterTypes.length != 1 || parameterTypes[0] != JoinPoint.class) {
                    throw new AopConfigException("The parameterType of before or after advice has to be a 'JoinPoint'");
                }
                return this.adviceMethod.invoke(this.aspectInstance, (JoinPoint) proceedingJoinPoint);
            }
            if (adviceType == AdviceType.AfterReturning) {
                if (this.parameterTypes.length != 2 || parameterTypes[0] != JoinPoint.class || parameterTypes[1] != Object.class) {
                    throw new AopConfigException("The parameterTypes of afterReturning advice have to be 'JoinPoint' and 'Object'");
                }
                return this.adviceMethod.invoke(this.aspectInstance, (JoinPoint) proceedingJoinPoint, returnValue);
            }
            if (adviceType == AdviceType.AfterThrowing) {
                if (this.parameterTypes.length != 2 || parameterTypes[0] != JoinPoint.class || parameterTypes[1] != Throwable.class) {
                    throw new AopConfigException("The parameterTypes of afterThrowing advice have to be 'JoinPoint' and 'Throwable'");
                }
                return this.adviceMethod.invoke(this.aspectInstance, (JoinPoint) proceedingJoinPoint, ex);
            }
        }
        return null;
    }

    public boolean isBeforeAdvice() {
        return false;
    }

    public boolean isAfterAdvice() {
        return false;
    }
}
