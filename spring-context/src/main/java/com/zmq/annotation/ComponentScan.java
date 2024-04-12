package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Repeatable(ComponentScans.class)
public @interface ComponentScan {
    String[] value() default {};
}
