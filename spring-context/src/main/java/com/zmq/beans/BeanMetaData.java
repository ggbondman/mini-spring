package com.zmq.beans;

import com.zmq.annotation.Order;
import com.zmq.annotation.Primary;

/**
 * @author zmq
 */
public class BeanMetaData {

    private String beanName;

    private int order;

    private boolean primary;

    public BeanMetaData(Class<?> clazz) {
        this.order = getOrder(clazz);
        this.primary = isPrimary(clazz);

    }

    int getOrder(Class<?> clazz){
        Order order = clazz.getAnnotation(Order.class);
        return order.value();
    }

    boolean isPrimary(Class<?> clazz){
        return clazz.isAnnotationPresent(Primary.class);
    }




}
