package com.zmq.interceptor;

import com.zmq.aop.AbstractAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
public class AfterThrowingAdviceInterceptor extends AbstractAdvice implements MethodInterceptor {

    public AfterThrowingAdviceInterceptor(Method adviceMethod) {
        super(adviceMethod);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        } catch (Throwable e) {
            if (shouldInvokeOnThrowing(e)) {
                invokeAdviceMethod(invocation, null, e);
            }
            throw e;
        }
    }

    private boolean shouldInvokeOnThrowing(Throwable ex) {
        return getDiscoveredThrowingType().isAssignableFrom(ex.getClass());
    }
}
