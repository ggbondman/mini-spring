package com.zmq.util;

import com.zmq.aop.Advisor;
import com.zmq.aop.AdvisorImpl;
import com.zmq.aop.AspectjPointcut;
import com.zmq.beans.ClassMetaData;
import com.zmq.beans.DefaultClassMetaData;
import com.zmq.exception.AopConfigException;
import org.aspectj.lang.annotation.Aspect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.zmq.util.ReflectionUtils.*;

/**
 * @author zmq
 */
public class AopUtils {

    private static final List<Advisor> advisors = new ArrayList<>();

    public static List<Advisor> getAdvisors(){
        return advisors;
    }

    public static void registerAdvisors(Class<?> clazz){
        ClassMetaData metaData = new DefaultClassMetaData(clazz);
        if (metaData.hasAnnotation(Aspect.class)) {
            for (Method method : getAdvisorMethods(clazz)) {
                Advisor advisor = getAdvisor(method, clazz);
                if (advisor!=null){
                    advisors.add(advisor);
                }
            }
        }
        advisors.sort(advisorComparator);
    }
    public static void registerAdvisor(Advisor advisor){
        advisors.add(advisor);
        advisors.sort(advisorComparator);
    }

    private static Advisor getAdvisor(Method method, Class<?> clazz) {
        Annotation aspectAnnotation = findAspectAnnotationOnMethod(method);
        if (aspectAnnotation == null) {
            return null;
        }
        String pointcutExpression = getPointCutByAspectAnnotation(aspectAnnotation);
        AspectjPointcut aspectjPointcut = new AspectjPointcut(pointcutExpression, clazz);
        return new AdvisorImpl(aspectAnnotation.annotationType(),method, aspectjPointcut);
    }

    private static String getPointCutByAspectAnnotation(Annotation aspectAnnotation){
        String pointcutExpression;
        try {
            Method value = aspectAnnotation.annotationType().getDeclaredMethod("value");
            pointcutExpression = (String)value.invoke(aspectAnnotation);
        } catch (ReflectiveOperationException e){
            throw new AopConfigException(e);
        }
        return pointcutExpression;
    }

    private static List<Method> getAdvisorMethods(Class<?> aspectClass) {
        List<Method> advisorMethods = new ArrayList<>();
        ReflectionUtils.doWithMethods(aspectClass, advisorMethods::add, adviceMethodFilter);
        return advisorMethods;
    }

}
