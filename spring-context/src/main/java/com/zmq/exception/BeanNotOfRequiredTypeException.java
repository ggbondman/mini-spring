package com.zmq.exception;

/**
 * @author zmq
 */
public class BeanNotOfRequiredTypeException extends BeansException{
    public BeanNotOfRequiredTypeException() {
    }

    public BeanNotOfRequiredTypeException(String message) {
        super(message);
    }
}
