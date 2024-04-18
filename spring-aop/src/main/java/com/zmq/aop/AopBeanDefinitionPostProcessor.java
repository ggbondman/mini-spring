package com.zmq.aop;

import com.zmq.beans.BeanDefinition;
import com.zmq.processor.BeanDefinitionPostProcessor;
import com.zmq.util.AopUtils;

/**
 * @author zmq
 */
public class AopBeanDefinitionPostProcessor implements BeanDefinitionPostProcessor {
    @Override
    public void invokeBeanDefinitionPostProcessor(BeanDefinition def) {
        AopUtils.registerAdvisors(def.getBeanClass());
    }
}
