package com.zmq.annotation;

import com.zmq.tx.DataSourceTransactionManager;

import java.lang.annotation.*;

/**
 * @author zmq
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({DataSourceTransactionManager.class})
public @interface EnableTransactionManagement {

}
