package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface Bean {
    String value() default "";
    String initMethod() default "";
}
