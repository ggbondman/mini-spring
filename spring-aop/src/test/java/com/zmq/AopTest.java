package com.zmq;

import com.zmq.annotation.SpringbootApplication;
import com.zmq.context.AnnotationConfigApplicationContext;
import com.zmq.controller.TestController;
import com.zmq.property.PropertyResolver;
import org.junit.jupiter.api.Test;

import java.util.Properties;

/**
 * @author zmq
 */
@SpringbootApplication
public class AopTest {

    @Test
    void testAllAspect(){
        AnnotationConfigApplicationContext context = null;
        context = new AnnotationConfigApplicationContext(AopTest.class, new PropertyResolver(new Properties()));
        TestController controller = context.getBean("TestController",TestController.class);
        controller.testAspect();
    }


}



