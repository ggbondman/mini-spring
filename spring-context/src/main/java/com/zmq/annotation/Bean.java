package com.zmq.annotation;

import java.lang.annotation.*;

/**
 * @author zmq
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface Bean {

    /**
     * bean的名称
     * @return {@link String}
     */
    String value() default "";

    /**
     * 初始化方法
     * @return {@link String}
     */
    String initMethod() default "";

    /**
     * 销毁方法
     * @return {@link String}
     */
    String destroyMethod() default "";
}
