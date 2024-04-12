package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Inherited
public @interface PathVariable {

    String value() default "";
}
