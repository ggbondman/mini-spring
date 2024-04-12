package com.zmq.exception;

/**
 * @author zmq
 */
public class ControllerParameterException extends BadRequestException{

    public ControllerParameterException() {
    }

    public ControllerParameterException(String message) {
        super(message);
    }

    public ControllerParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ControllerParameterException(Throwable cause) {
        super(cause);
    }
}
