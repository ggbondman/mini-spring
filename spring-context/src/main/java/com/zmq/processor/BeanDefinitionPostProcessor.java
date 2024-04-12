package com.zmq.processor;

import com.zmq.context.ApplicationContext;

/**
 * @author zmq
 */
public interface BeanDefinitionPostProcessor {

    void invokeBeanDefinitionPostProcessor(ApplicationContext applicationContext);
}
