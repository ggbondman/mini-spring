package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Service {

    String value() default "";
}
