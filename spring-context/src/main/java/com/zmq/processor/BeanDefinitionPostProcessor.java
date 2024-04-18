package com.zmq.processor;

import com.zmq.beans.BeanDefinition;

/**
 * 预留的拓展点，可以通过实现该接口在BeanDefinition创建后，实例化Bean之前修改BeanDefinition
 * @author zmq
 */
public interface BeanDefinitionPostProcessor {

    void invokeBeanDefinitionPostProcessor(BeanDefinition def);
}
