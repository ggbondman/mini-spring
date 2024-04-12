package com.zmq.annotation;

import com.zmq.aop.AopBeanDefinitionPostProcessor;

import java.lang.annotation.*;

/**
 * 开启AOP的注解
 * @author zmq
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({AopBeanDefinitionPostProcessor.class})
public @interface EnableAspectJAutoProxy {

}
