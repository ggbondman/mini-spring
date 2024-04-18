package com.zmq.beans;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author zmq
 */
public class DefaultClassMetaData implements ClassMetaData {

    private final Class<?> clazz;

    Set<Class<?>> allAnnotations = new HashSet<>();
    Map<Class<?>, AnnotationMapping> annotationsMap = new HashMap<>();
    Map<Class<?>, List<Method>> methodAnnotationsMap = new HashMap<>();
    Map<Class<?>, List<Field>> fieldAnnotationsMap = new HashMap<>();

    public DefaultClassMetaData(Class<?> clazz) {
        this.clazz = clazz;
        this.annotationsMap = findAllAnnotations(this.clazz);
        findAllMethodAnnotations(this.clazz);
        findAllFieldAnnotations(this.clazz);
    }

    private Map<Class<?>, AnnotationMapping> findAllAnnotations(Class<?> annotationClass) {
        Map<Class<?>, AnnotationMapping> map = new HashMap<>();
        for (Annotation annotation : annotationClass.getAnnotations()) {
            Class<? extends Annotation> clazz = annotation.annotationType();
            if (!clazz.getPackageName().equals("java.lang.annotation")) {
                this.allAnnotations.add(clazz);
                map.put(clazz, new AnnotationMapping(annotation, findAllAnnotations(clazz)));
            }
        }
        return map;
    }

    private void findAllMethodAnnotations(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation annotation : annotations) {
                method.setAccessible(true);
                List<Method> list = this.methodAnnotationsMap.getOrDefault(annotation.annotationType(), new ArrayList<>());
                list.add(method);
                this.methodAnnotationsMap.put(annotation.annotationType(), list);
            }
        }
        findAllMethodAnnotations(clazz.getSuperclass());
    }

    private void findAllFieldAnnotations(Class<?> clazz) {
        if (clazz == null) {
            return;
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getAnnotations();
            for (Annotation annotation : annotations) {
                List<Field> list = this.fieldAnnotationsMap.getOrDefault(annotation.annotationType(), new ArrayList<>());
                list.add(field);
                this.fieldAnnotationsMap.put(annotation.annotationType(), list);
            }
        }
        findAllFieldAnnotations(clazz.getSuperclass());
    }


    private Annotation searchAnnotationMap(AnnotationMapping mapping, Class<?> annotationClass) {
        if (mapping == null) {
            return null;
        }
        Map<Class<?>, AnnotationMapping> parentAnnotations = mapping.parentAnnotations;
        if (parentAnnotations==null){
            return null;
        }
        if (parentAnnotations.containsKey(annotationClass)) {
            return parentAnnotations.get(annotationClass).annotation;
        }
        for (AnnotationMapping value : parentAnnotations.values()) {
            return searchAnnotationMap(value, annotationClass);
        }
        return null;
    }

    @Override
    public boolean hasAnnotation(Class<?> clazz) {
        return this.allAnnotations.contains(clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> A getAnnotation(Class<A> clazz) {
        if (this.annotationsMap.containsKey(clazz)) {
            return (A) this.annotationsMap.get(clazz).annotation;
        }
        for (AnnotationMapping value : this.annotationsMap.values()) {
            Annotation annotation = searchAnnotationMap(value, clazz);
            if (annotation!=null){
                return (A) annotation;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends Annotation> List<A> getAnnotations(Class<A> clazz) {
        List<A> annotationList = new ArrayList<>();
        if (this.annotationsMap.containsKey(clazz)) {
            annotationList.add((A) this.annotationsMap.get(clazz).annotation);
        }
        for (AnnotationMapping value : this.annotationsMap.values()) {
            Annotation annotation = searchAnnotationMap(value, clazz);
            if (annotation!=null) {
                annotationList.add((A) annotation);
            }
        }
        return annotationList;
    }

    @Override
    public <A extends Annotation> boolean hasMethodAnnotation(Class<A> clazz) {
        return this.methodAnnotationsMap.containsKey(clazz);
    }

    @Override
    public <A extends Annotation> Method[] getMethodsByAnnotation(Class<A> clazz) {

        return this.methodAnnotationsMap.containsKey(clazz) ? this.methodAnnotationsMap.get(clazz).toArray(new Method[0]) : new Method[0];
    }

    @Override
    public <A extends Annotation> boolean hasFieldAnnotation(Class<A> clazz) {
        return this.fieldAnnotationsMap.containsKey(clazz);
    }

    @Override
    public <A extends Annotation> Field[] getFieldsByAnnotation(Class<A> clazz) {
        return this.fieldAnnotationsMap.containsKey(clazz) ? this.fieldAnnotationsMap.get(clazz).toArray(new Field[0]) : new Field[0];
    }

    @Override
    public String getClassName() {
        return this.clazz.getSimpleName();
    }

    @Override
    public Class<?> getIntrospectedClass() {
        return this.clazz;
    }


    @Override
    public boolean isInterface() {
        return this.clazz.isInterface();
    }

    @Override
    public boolean isAnnotation() {
        return this.clazz.isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(this.clazz.getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(this.clazz.getModifiers());
    }

    @Override
    public boolean isRecord() {
        return this.clazz.isRecord();
    }

    @Override
    public boolean isEnum() {
        return this.clazz.isEnum();
    }

    @Override
    public boolean isPrivate() {
        return Modifier.isPrivate(this.clazz.getModifiers());
    }



    private record AnnotationMapping(Annotation annotation, Map<Class<?>, AnnotationMapping> parentAnnotations) {
    }
}

