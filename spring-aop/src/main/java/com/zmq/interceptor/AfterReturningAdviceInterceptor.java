package com.zmq.interceptor;

import com.zmq.aop.AbstractAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
public class AfterReturningAdviceInterceptor extends AbstractAdvice implements MethodInterceptor {


    public AfterReturningAdviceInterceptor(Method adviceMethod) {
        super(adviceMethod);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object returnValue = invocation.proceed();
        invokeAdviceMethod(invocation,returnValue,null);
        return returnValue;
    }
}
