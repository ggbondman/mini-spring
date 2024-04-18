package com.zmq.util;

import com.zmq.aop.AdviceType;
import com.zmq.aop.Advisor;
import jakarta.annotation.Nullable;
import org.aspectj.lang.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.Map;

/**
 * 跟反射有关的一些工具
 * @author zmq
 */
public abstract class ReflectionUtils {
    public static final Map<Class<?>, AdviceType> annotationTypeMap = Map.of(
            Pointcut.class, AdviceType.Pointcut, //
            Around.class, AdviceType.Around, //
            Before.class, AdviceType.Before, //
            After.class, AdviceType.After, //
            AfterReturning.class, AdviceType.AfterReturning, //
            AfterThrowing.class, AdviceType.AfterThrowing //
    );

    public static final Class<? extends Annotation>[] ASPECT_ANNOTATION_CLASSES = new Class[]{Pointcut.class, Around.class, Before.class, After.class, AfterReturning.class, AfterThrowing.class};

    public static final MethodFilter USER_DECLARED_METHODS =
            (method -> !method.isBridge() && !method.isSynthetic() && (method.getDeclaringClass() != Object.class));
    public static final MethodFilter adviceMethodFilter = ReflectionUtils.USER_DECLARED_METHODS
            .and(method -> !method.isAnnotationPresent(Pointcut.class));

    public static final Comparator<Advisor> advisorComparator = (o1, o2) -> {
        AdviceTypeComparator<Object> adviceTypeComparator = new AdviceTypeComparator<>(AdviceType.Around,AdviceType.Before,AdviceType.After,AdviceType.AfterReturning,AdviceType.AfterThrowing);
        return adviceTypeComparator.compare(o1.getAdviceType(), o2.getAdviceType());
    };




    public static void doWithMethods(Class<?> clazz, MethodCallback callback, @Nullable MethodFilter filter) {
        if (filter == USER_DECLARED_METHODS && clazz == Object.class) {
            // nothing to introspect
            return;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (filter != null && !filter.matches(method)) {
                continue;
            }
            try {
                callback.doWith(method);
            }
            catch (IllegalAccessException ex) {
                throw new IllegalStateException("Not allowed to access method '"+method.getName()+"': "+ex);
            }
        }
        // Keep backing up the inheritance hierarchy.
        if (clazz.getSuperclass() != null && (filter != USER_DECLARED_METHODS || clazz.getSuperclass() != Object.class)) {
            doWithMethods(clazz.getSuperclass(), callback, filter);
        }
        else if (clazz.isInterface()) {
            for (Class<?> superIfc : clazz.getInterfaces()) {
                doWithMethods(superIfc, callback, filter);
            }
        }
    }

    public static AdviceType findAspectAnnotationTypeOnMethod(Method method) {
        for (Class<? extends Annotation> annotationType : ASPECT_ANNOTATION_CLASSES) {
            Annotation annotation = method.getAnnotation(annotationType);
            if (annotation != null) {
                return annotationTypeMap.get(annotationType);
            }
        }
        return null;
    }

    public static Annotation findAspectAnnotationOnMethod(Method method) {
        for (Class<? extends Annotation> annotationType : ASPECT_ANNOTATION_CLASSES) {
            Annotation annotation = method.getAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface MethodCallback {

        /**
         * Perform an operation using the given method.
         * @param method the method to operate on
         */
        void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
    }
    @FunctionalInterface
    public interface MethodFilter {

        /**
         * Determine whether the given method matches.
         * @param method the method to check
         */
        boolean matches(Method method);

        /**
         * Create a composite filter based on this filter <em>and</em> the provided filter.
         * <p>If this filter does not match, the next filter will not be applied.
         * @param next the next {@code MethodFilter}
         * @return a composite {@code MethodFilter}
         * @throws IllegalArgumentException if the MethodFilter argument is {@code null}
         * @since 5.3.2
         */
        default MethodFilter and(MethodFilter next) {
            return method -> matches(method) && next.matches(method);
        }
    }
}
