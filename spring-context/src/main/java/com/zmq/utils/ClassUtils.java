package com.zmq.utils;

import java.lang.annotation.Annotation;
import java.time.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zmq
 */
public class ClassUtils {

    public final static Map<Class<?>, Function<String, Object>> CLASS_CONVERTER = new HashMap<>() {
        {
            put(String.class,String::toString);
            put(boolean.class, Boolean::parseBoolean);
            put(Boolean.class, Boolean::valueOf);

            put(byte.class, Byte::parseByte);
            put(Byte.class, Byte::valueOf);

            put(short.class, Short::parseShort);
            put(Short.class, Short::valueOf);

            put(int.class, Integer::parseInt);
            put(Integer.class, Integer::valueOf);

            put(long.class, Long::parseLong);
            put(Long.class, Long::valueOf);

            put(float.class, Float::parseFloat);
            put(Float.class, Float::valueOf);

            put(double.class, Double::parseDouble);
            put(Double.class, Double::valueOf);

            put(LocalDate.class, LocalDate::parse);
            put(LocalTime.class, LocalTime::parse);
            put(LocalDateTime.class, LocalDateTime::parse);
            put(ZonedDateTime.class, ZonedDateTime::parse);
            put(Duration.class, Duration::parse);
            put(ZoneId.class, ZoneId::of);
        }
    };

    public static <A extends Annotation> A findAnnotation(Class<?> configClass, Class<A> annotationClass) {
        A annotation = configClass.getAnnotation(annotationClass);
        if (annotation == null) {
            for (Annotation otherAnnotation : configClass.getAnnotations()) {
                // 如果不是java自带的注解，才会递归查找
                if (!otherAnnotation.annotationType().getPackageName().equals("java.lang.annotation")) {
                    annotation = findAnnotation(otherAnnotation.annotationType(), annotationClass);
                    if (annotation!=null) return annotation;
                }
            }
        }
        return annotation;
    }

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        }
        catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                }
                catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }



}
