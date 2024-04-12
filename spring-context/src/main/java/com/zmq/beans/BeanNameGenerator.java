package com.zmq.beans;

import com.zmq.annotation.Component;
import com.zmq.utils.ClassUtils;

import java.util.Objects;

/**
 * @author zmq
 */
public class BeanNameGenerator {

    public static String generate(Class<?> clazz){
        Component component = ClassUtils.findAnnotation(clazz, Component.class);
        if (!Objects.equals(component.value(), "")){
            return component.value();
        }
        return clazz.getSimpleName().toLowerCase();
    }


}
