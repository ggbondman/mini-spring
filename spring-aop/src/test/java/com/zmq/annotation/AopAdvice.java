package com.zmq.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import static java.lang.System.out;

/**
 * @author 郑铭强
 */
//@Aspect
//@Component
public class AopAdvice {
    @Pointcut("execution (* com.zmq.annotation.*(..))")
    public void test() {

    }

    @Before("execution (* com.zmq.annotation.*.*(..))")
    public void beforeAdvice() {
        out.println("beforeAdvice...");
    }

    @After("execution (* com.zmq.annotation.*.*(..))")
    public void afterAdvice() {
        out.println("afterAdvice...");
    }

    @Around("execution (* com.zmq.annotation.*.*(..))")
    public void aroundAdvice(ProceedingJoinPoint proceedingJoinPoint) {
        out.println("before");
        try {
            proceedingJoinPoint.proceed();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        out.println("after");
    }

    public void ss(){
        out.println("lala");
    }
}
