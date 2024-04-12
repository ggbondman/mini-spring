package com.zmq.processor;

import com.zmq.annotation.Component;
import com.zmq.beans.ClassMetaData;
import com.zmq.beans.DefaultClassMetaData;
import com.zmq.exception.BeanCreationException;
import jakarta.annotation.PostConstruct;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
@Component
public class InitDestroyAnnotationBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        ClassMetaData metaData = new DefaultClassMetaData(bean.getClass());
        if (metaData.hasMethodAnnotation(PostConstruct.class)){
            Method[] method = metaData.getMethodsByAnnotation(PostConstruct.class);
            for (Method m : method) {
                try {
                    m.invoke(bean);
                } catch (ReflectiveOperationException e){
                    throw new BeanCreationException(e);
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
