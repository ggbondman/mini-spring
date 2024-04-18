package com.zmq.exception;

/**
 * @author zmq
 */
public class BeanInitializingException extends BeansException {
    public BeanInitializingException() {
    }

    public BeanInitializingException(String message) {
        super(message);
    }

    public BeanInitializingException(Throwable cause) {
        super(cause);
    }

    public BeanInitializingException(String message, Throwable cause) {
        super(message, cause);
    }
}
