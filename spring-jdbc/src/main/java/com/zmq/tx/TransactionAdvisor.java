package com.zmq.tx;

import com.zmq.aop.AdviceType;
import com.zmq.aop.Advisor;
import com.zmq.aop.AspectjPointcut;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
public class TransactionAdvisor implements Advisor {

    private final AspectjPointcut pointcut;

    private final MethodInterceptor advice;

    public TransactionAdvisor(Class<?> clazz, DataSourceTransactionManager transactionManager) {
        String expression = getPointcutExpression(clazz);
        this.pointcut =  new AspectjPointcut(expression,clazz);
        this.advice = transactionManager;
    }


    public TransactionAdvisor(Method method, DataSourceTransactionManager transactionManager) {
        String expression = getPointcutExpression(method);
        this.pointcut =  new AspectjPointcut(expression,method.getDeclaringClass());
        this.advice = transactionManager;
    }

    private String getPointcutExpression(Class<?> clazz){
        return "execution (* "+clazz.getName()+".*(..))";
    }
    private String getPointcutExpression(Method method){
        return "execution ("+method.toGenericString()+")";
    }



    @Override
    public MethodInterceptor getAdvice() {
        return this.advice;
    }

    @Override
    public AspectjPointcut getPointcut() {
        return pointcut;
    }

    @Override
    public AdviceType getAdviceType() {
        return AdviceType.Transactional;
    }
}
