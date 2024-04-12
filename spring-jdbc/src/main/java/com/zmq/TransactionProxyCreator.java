package com.zmq;

/**
 * @author zmq
 */
public class TransactionProxyCreator {

    private Class<?> targetClass;

    private Object target;

    public TransactionProxyCreator(Class<?> targetClass, Object target) {
        this.targetClass = targetClass;
        this.target = target;
    }

    public Object createProxy(){
        return null;
    }
}
