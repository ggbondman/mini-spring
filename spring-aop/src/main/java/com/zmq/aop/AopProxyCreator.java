package com.zmq.aop;

import lombok.Data;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodProxy;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author zmq
 */
@Data
public class AopProxyCreator {

    private Class<?> targetClass;

    private Object target;

    private List<Advisor> advisors;

    public AopProxyCreator(Class<?> targetClass, Object target, List<Advisor> advisors) {
        this.targetClass = targetClass;
        this.target = target;
        this.advisors = advisors;
    }

    public Object createProxy() {
        Callback aopInterceptor = new CglibInterceptor(this.targetClass,this.target,this.advisors);
        return Enhancer.create(targetClass, aopInterceptor);
    }


    private static class CglibInterceptor implements net.sf.cglib.proxy.MethodInterceptor{
        Class<?> targetClass;
        Object target;
        List<Advisor> advisors;

        public CglibInterceptor(Class<?> targetClass, Object target, List<Advisor> advisors) {
            this.targetClass = targetClass;
            this.target = target;
            this.advisors = advisors;
        }

        @Override
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            List<MethodInterceptor> interceptors = advisors.stream().filter(advisor -> advisor.getPointcut().matchMethod(method)).map(Advisor::getAdvice).toList();
            return new ReflectiveMethodInvocation(proxy,target,method,args,methodProxy,interceptors).proceed();
        }
    }




}
