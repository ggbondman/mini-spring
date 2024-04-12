package com.zmq.exception;

/**
 * @author zmq
 */
public class BeansException extends RuntimeException{
    public BeansException() {
    }

    public BeansException(String message) {
        super(message);
    }

    public BeansException(Throwable cause) {
        super(cause);
    }

    public BeansException(String message, Throwable cause) {
        super(message, cause);
    }
}
