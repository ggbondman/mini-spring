package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD,ElementType.METHOD,ElementType.PARAMETER})
@Inherited
public @interface Autowired {
    String name() default "";
    boolean value() default false;
}
