package com.zmq.exception;

/**
 * @author zmq
 */
public class BeanDestroyException extends RuntimeException{

    public BeanDestroyException() {
    }

    public BeanDestroyException(String message) {
        super(message);
    }

    public BeanDestroyException(String message, Throwable cause) {
        super(message, cause);
    }

    public BeanDestroyException(Throwable cause) {
        super(cause);
    }
}
