package com.zmq.exception;

/**
 * @author zmq
 */
public class BeanDefinitionException extends RuntimeException{

    public BeanDefinitionException() {
    }

    public BeanDefinitionException(String message) {
        super(message);
    }

    public BeanDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanDefinitionException(Throwable cause) {
        super(cause);
    }
}
