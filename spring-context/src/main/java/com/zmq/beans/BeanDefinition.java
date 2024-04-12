package com.zmq.beans;

import com.zmq.annotation.Bean;
import com.zmq.annotation.Configuration;
import com.zmq.annotation.Order;
import com.zmq.annotation.Primary;
import com.zmq.processor.BeanDefinitionPostProcessor;
import com.zmq.processor.BeanPostProcessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * @author zmq
 */
@Data
public class BeanDefinition {

    /**
     * 全局唯一的Bean Name
     */
    String name;
    /**
     * Bean的声明类型
     */
    Class<?> beanClass;

    String beanClassName;
    /**
     * Bean的实例
     */
    Object instance = null;
    /**
     * 工厂方法名
     */
    String factoryBeanName;
    /**
     * 工厂方法
     */
    Method factoryMethod;
    /**
     * Bean的顺序
     */
    int order;
    /**
     * 是否带有@Primary注解
     */
    boolean primary;

    boolean configuration;

    boolean beanDefinitionPostProcessor;

    boolean beanPostProcessor;
    /**
     *  init方法名
     */
    String initMethodName;
    /**
     *  init方法
     */
    Method initMethod;
    /**
     *  destroy方法名
     */
    String destroyMethodName;
    /**
     *  destroy方法
     */
    Method destroyMethod;


    public BeanDefinition(ClassMetaData classMetaData) {
        this.name = StringUtils.uncapitalize(classMetaData.getClassName());
        this.beanClass = classMetaData.getIntrospectedClass();
        this.beanClassName = this.beanClass.getName();
        this.order = classMetaData.hasAnnotation(Order.class)?classMetaData.getAnnotation(Order.class).value():0;
        this.primary = classMetaData.hasAnnotation(Primary.class);
        this.configuration = classMetaData.hasAnnotation(Configuration.class);
        this.beanPostProcessor = BeanPostProcessor.class.isAssignableFrom(classMetaData.getIntrospectedClass());
        this.beanDefinitionPostProcessor = BeanDefinitionPostProcessor.class.isAssignableFrom(classMetaData.getIntrospectedClass());
        this.initMethod = classMetaData.hasMethodAnnotation(PostConstruct.class)?classMetaData.getMethodsByAnnotation(PostConstruct.class)[0]:null;
        this.destroyMethod = classMetaData.hasMethodAnnotation(PreDestroy.class)?classMetaData.getMethodsByAnnotation(PreDestroy.class)[0]:null;

    }

    public BeanDefinition(ClassMetaData classMetaData,Method factoryMethod,String factoryBeanName) {
        this(classMetaData);

        this.name = getBeanName(factoryMethod);
        this.factoryMethod = factoryMethod;
        this.factoryBeanName = factoryBeanName;
    }

    private String getBeanName(Method factoryMethod){
        Bean bean = factoryMethod.getAnnotation(Bean.class);
        String name = bean.value();
        if (name.isEmpty()) {
            name = factoryMethod.getName();
        }
        return name;
    }


}
