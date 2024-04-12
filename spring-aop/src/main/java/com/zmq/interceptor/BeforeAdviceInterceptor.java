package com.zmq.interceptor;

import com.zmq.aop.AbstractAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
public class BeforeAdviceInterceptor extends AbstractAdvice implements MethodInterceptor {

    public BeforeAdviceInterceptor(Method adviceMethod) {
        super(adviceMethod);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        invokeAdviceMethod(methodInvocation,null,null);
        return methodInvocation.proceed();
    }
}
