package com.zmq.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author zmq
 */
public interface AnnotationMetaData {


    boolean hasAnnotation(Class<?> clazz);
    <A extends Annotation> A getAnnotation(Class<A> clazz);
    <A extends Annotation> List<A> getAnnotations(Class<A> clazz);
    <A extends Annotation> boolean hasMethodAnnotation(Class<A> clazz);
    <A extends Annotation> Method[] getMethodsByAnnotation(Class<A> clazz);
    <A extends Annotation> boolean hasFieldAnnotation(Class<A> clazz);
    <A extends Annotation> Field[] getFieldsByAnnotation(Class<A> clazz);

}
