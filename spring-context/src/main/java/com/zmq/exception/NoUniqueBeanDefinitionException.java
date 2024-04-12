package com.zmq.exception;

/**
 * @author zmq
 */
public class NoUniqueBeanDefinitionException extends BeansException{
    public NoUniqueBeanDefinitionException() {
    }

    public NoUniqueBeanDefinitionException(String message) {
        super(message);
    }
}
