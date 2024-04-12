package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Inherited
public @interface RequestParam {

    String value() default "";

    String defaultValue() default "\n\t\t\n\t\t\n\uE000\uE001\uE002\n\t\t\t\t\n";
}
