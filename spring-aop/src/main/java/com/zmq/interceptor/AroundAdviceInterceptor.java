package com.zmq.interceptor;

import com.zmq.aop.AbstractAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
public class AroundAdviceInterceptor extends AbstractAdvice implements MethodInterceptor {

    public AroundAdviceInterceptor(Method adviceMethod) {
        super(adviceMethod);
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        return invokeAdviceMethod(methodInvocation,null,null);
    }
}
