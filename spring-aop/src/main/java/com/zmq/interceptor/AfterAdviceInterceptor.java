package com.zmq.interceptor;

import com.zmq.aop.AbstractAdvice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
public class AfterAdviceInterceptor extends AbstractAdvice implements MethodInterceptor {

    public AfterAdviceInterceptor(Method adviceMethod) {
        super(adviceMethod);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        try {
            return invocation.proceed();
        }
        finally {
            invokeAdviceMethod(invocation,null,null);
        }
    }
}
