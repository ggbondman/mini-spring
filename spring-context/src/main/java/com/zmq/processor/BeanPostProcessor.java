package com.zmq.processor;

import com.zmq.annotation.Orderd;

/**
 * @author zmq
 */
public interface BeanPostProcessor extends Orderd {

    /**
     * 将Bean实例放到容器之前触发
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Invoked after bean.init() called.
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    default int getOrder(){
        return 0;
    }
}
