package com.zmq.beans;

/**
 * @author zmq
 */
public interface ClassMetaData extends AnnotationMetaData {
    /**
     * Return the name of the underlying class.
     */
    String getClassName();

    Class<?> getIntrospectedClass();

    /**
     * Return whether the underlying class represents an interface.
     */
    boolean isInterface();

    /**
     * Return whether the underlying class represents an annotation.
     * @since 4.1
     */
    boolean isAnnotation();

    /**
     * Return whether the underlying class is marked as abstract.
     */
    boolean isAbstract();

    /**
     * Return whether the underlying class is marked as 'final'.
     */
    boolean isFinal();
    boolean isEnum();
    boolean isRecord();
    boolean isPrivate();




}
