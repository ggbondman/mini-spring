package com.zmq.exception;

/**
 * @author zmq
 */
public class NoSuchBeanDefinitionException extends BeansException {

    public NoSuchBeanDefinitionException() {
    }

    public NoSuchBeanDefinitionException(String message) {
        super(message);
    }
}
