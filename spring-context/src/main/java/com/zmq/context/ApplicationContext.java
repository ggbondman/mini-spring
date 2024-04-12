package com.zmq.context;

import com.zmq.beans.BeanDefinition;

import java.util.List;
import java.util.Map;

/**
 * @author zmq
 */
public interface ApplicationContext extends AutoCloseable {
    // 是否存在指定name的Bean
    boolean containsBean(String name);

    // 根据name返回唯一Bean，未找到抛出NoSuchBeanDefinitionException
    <T> T getBean(String name);

    // 根据name返回唯一Bean，未找到抛出NoSuchBeanDefinitionException
    <T> T getBean(String name, Class<T> requiredType);

    // 根据type返回唯一Bean，未找到抛出NoSuchBeanDefinitionException
    <T> T getBean(Class<T> requiredType);

    Map<String,Object> getBeans();

    <T> List<T> getBeans(Class<T> clazz);
    String[] getBeanNames();

    List<BeanDefinition> getAllBeanDefinitions();

    // 关闭并执行所有bean的destroy方法
    @Override
    void close();

}
