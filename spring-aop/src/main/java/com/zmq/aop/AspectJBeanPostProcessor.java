package com.zmq.aop;

import com.zmq.annotation.Component;
import com.zmq.processor.BeanPostProcessor;
import com.zmq.util.AopUtils;
import org.aspectj.lang.annotation.Aspect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zmq
 */
@Component
public class AspectJBeanPostProcessor implements BeanPostProcessor {
    private static final Map<String,Object> TARGET_MAP = new HashMap<>();
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
        TARGET_MAP.put(beanName,bean);
        if (selected.isEmpty()){
            return bean;
        }
        return new AopProxyCreator(bean.getClass(),bean,selected).createProxy();
    }

    @Override
    public Object getOriginTarget(Object bean, String beanName) {
        return TARGET_MAP.getOrDefault(beanName,bean);
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }
}
