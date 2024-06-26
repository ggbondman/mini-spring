package com.zmq.context;

import com.zmq.beans.BeanDefinition;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * @author zmq
 */
public interface ConfigurableApplicationContext extends ApplicationContext{

    List<BeanDefinition> findBeanDefinitions(Class<?> type);

    @Nullable
    BeanDefinition findBeanDefinition(String name);

    @Nullable
    BeanDefinition findBeanDefinition(String name, Class<?> requiredType);

    Object createBeanAsEarlySingleton(BeanDefinition def);
}
