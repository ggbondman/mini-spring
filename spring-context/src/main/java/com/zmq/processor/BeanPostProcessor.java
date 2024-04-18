package com.zmq.processor;

import com.zmq.Orderd;

/**
 * 预留的拓展点，可以通过实现该接口在Bean的生命周期内操作Bean
 * @author zmq
 */
public interface BeanPostProcessor extends Orderd {

    /**
     * Bean实例化后，初始化前触发
     */
    default Object postProcessBeforeInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * Bean初始化后触发
     */
    default Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    /**
     * 用于在生成代理对象后，通过此方法获得被代理的原始对象
     */
    default Object getOriginTarget(Object bean, String beanName){
        return bean;
    }


    /**
     * 定义PostPostProcessor的优先级
     */
    default int getOrder(){
        return 0;
    }
}
