package com.zmq.aop;

import org.aopalliance.intercept.MethodInterceptor;

/**
 * 保存AOP advice（在连接点采取的操作）和确定合适的的过滤器（如切入点）的基本接口。
 * 这个接口不是供Spring用户使用的，而是为了在支持不同类型的advice方面实现通用性。
 * Spring AOP基于通过方法拦截器提供的around advice，这符合AOP联盟拦截API。
 * Advisor接口允许支持不同类型的advice，例如建议before和after advice，
 * 这些advice不需要使用拦截来实现。
 * @author zmq
 */
public interface Advisor {
    
    MethodInterceptor getAdvice();

    AspectjPointcut getPointcut();

    AdviceType getAdviceType();

}
