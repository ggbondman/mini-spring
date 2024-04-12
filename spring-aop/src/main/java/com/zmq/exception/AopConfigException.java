package com.zmq.exception;

/**
 * @author zmq
 */
public class AopConfigException extends RuntimeException{

    public AopConfigException() {
        super();
    }

    public AopConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public AopConfigException(String message) {
        super(message);
    }

    public AopConfigException(Throwable cause) {
        super(cause);
    }
}
