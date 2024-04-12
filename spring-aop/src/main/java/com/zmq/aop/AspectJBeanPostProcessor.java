package com.zmq.aop;

import com.zmq.annotation.Component;
import com.zmq.processor.BeanPostProcessor;
import com.zmq.util.AopUtils;
import org.aspectj.lang.annotation.Aspect;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zmq
 */
@Component
public class AspectJBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        List<Advisor> selected = new ArrayList<>();
        for (Advisor advisor : AopUtils.getAdvisors()) {
            if (advisor.getPointcut().matchClass(bean.getClass())) {
                if (!bean.getClass().isAnnotationPresent(Aspect.class)) {
                    selected.add(advisor);
                }
            }
        }
        if (selected.isEmpty()){
            return bean;
        }
        return new AopProxyCreator(bean.getClass(),bean,selected).createProxy();
    }
}
