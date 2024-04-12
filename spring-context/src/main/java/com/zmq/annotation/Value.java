package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD,ElementType.TYPE_PARAMETER,ElementType.PARAMETER})
@Inherited
public @interface Value {
    String value();
}
