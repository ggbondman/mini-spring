package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Around {
    Class<?> value();
}
