package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Inherited
public @interface ResponseBody {
}
