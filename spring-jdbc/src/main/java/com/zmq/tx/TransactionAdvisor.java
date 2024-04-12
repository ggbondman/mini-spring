package com.zmq.tx;

import com.zmq.aop.AdviceType;
import com.zmq.aop.Advisor;
import com.zmq.aop.AspectjPointcut;
import org.aopalliance.intercept.MethodInterceptor;

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

    private String getPointcutExpression(Class<?> clazz){
        return "execution (* "+clazz.getName()+".*(..))";
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
