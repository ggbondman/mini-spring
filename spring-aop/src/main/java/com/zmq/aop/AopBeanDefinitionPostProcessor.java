package com.zmq.aop;

import com.zmq.beans.BeanDefinition;
import com.zmq.context.ApplicationContext;
import com.zmq.processor.BeanDefinitionPostProcessor;
import com.zmq.util.AopUtils;

/**
 * @author zmq
 */
public class AopBeanDefinitionPostProcessor implements BeanDefinitionPostProcessor {

    @Override
    public void invokeBeanDefinitionPostProcessor(ApplicationContext applicationContext) {
        for (BeanDefinition def : applicationContext.getAllBeanDefinitions()) {
            AopUtils.registerAdvisors(def.getBeanClass());
        }
    }
}
