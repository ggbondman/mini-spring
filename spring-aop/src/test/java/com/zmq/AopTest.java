package com.zmq;

import com.zmq.annotation.Component;
import com.zmq.annotation.EnableAspectJAutoProxy;
import com.zmq.context.AnnotationConfigApplicationContext;
import com.zmq.controller.TestController;
import com.zmq.property.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author zmq
 */
@Component
@EnableAspectJAutoProxy
public class AopTest {

    @Test
    void testAllAspect(){
        try(AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AopTest.class, new PropertyResolver(new Properties()))){
            TestController controller = context.getBean("testController", TestController.class);
            controller.testAspect();
        }

    }
}



