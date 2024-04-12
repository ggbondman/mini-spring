package com.zmq.annotation;

import com.zmq.RequestMethod;

import java.lang.annotation.*;

/**
 * @author zmq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Inherited
public @interface RequestMapping {
    RequestMethod[] method() default {};

    String value() default "";
}
