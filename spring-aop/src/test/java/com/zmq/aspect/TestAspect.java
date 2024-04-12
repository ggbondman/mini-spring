package com.zmq.aspect;

import com.zmq.annotation.Component;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

import static java.lang.System.out;

/**
 * @author zmq
 */
@Aspect
@Component
public class TestAspect {

    @Before("execution (* com.zmq.controller.*.*(..))")
    public void before(JoinPoint joinPoint){
        out.println("进入before：Class Method   : " + joinPoint.getSignature().getDeclaringTypeName()+" "+joinPoint.getSignature().getName());
    }

    @Around("execution (* com.zmq.controller.*.*(..))")
    public Object around(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        out.println("进入around");
        Object result = proceedingJoinPoint.proceed();
        out.println(result);
        out.println("退出around");
        return result;
    }

    @After("execution (* com.zmq.controller.*.*(..))")
    public void after(JoinPoint joinPoint) throws Throwable {
        out.println("进入after："+ joinPoint.getSignature().getDeclaringTypeName());
    }

    @AfterReturning("execution (* com.zmq.controller.*.*(..))")
    public void afterReturning(JoinPoint joinPoint,Object returnValue) throws Throwable {
        out.println("进入afterReturning："+ returnValue);
    }

    @AfterThrowing("execution (* com.zmq.controller.*.*(..))")
    public void afterThrowing(JoinPoint joinPoint,Throwable ex) throws Throwable {
        out.println("进入afterThrowing："+ ex);
    }
}
