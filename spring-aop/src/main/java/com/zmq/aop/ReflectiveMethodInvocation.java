package com.zmq.aop;

import jakarta.annotation.Nullable;
import net.sf.cglib.proxy.MethodProxy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author zmq
 */
public class ReflectiveMethodInvocation implements ProxyMethodInvocation {
    private final Object proxy;

    private final Object target;

    private final Method method;
    private final Object[] args;
    private final MethodProxy methodProxy;

    private final List<MethodInterceptor> interceptors;

    private int advisorIndex = 0;

    public ReflectiveMethodInvocation(Object proxy, Object target, Method method, Object[] args, MethodProxy methodProxy, List<MethodInterceptor> interceptors) {
        this.proxy = proxy;
        this.target = target;
        this.method = method;
        this.args = args;
        this.methodProxy = methodProxy;
        this.interceptors = interceptors;
    }

    @Override
    public Object getProxy() {
        return this.proxy;
    }

    @Override
    public MethodInvocation invocableClone() {
        return null;
    }

    @Override
    public MethodInvocation invocableClone(Object... arguments) {
        return null;
    }

    @Override
    public void setArguments(Object... arguments) {

    }

    @Override
    public void setUserAttribute(String key, @Nullable Object value) {

    }

    @Nullable
    @Override
    public Object getUserAttribute(String key) {
        return null;
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object[] getArguments() {
        return this.args;
    }

    @Override
    public Object proceed() throws Throwable {
        if (advisorIndex < interceptors.size()) {
            return interceptors.get(advisorIndex++).invoke(this);
        }
        return methodProxy.invoke(this.target,args);
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return this.method;
    }
}
