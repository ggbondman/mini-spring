package com.zmq.controller;

import com.zmq.annotation.Component;
import com.zmq.exception.AopConfigException;

/**
 * @author zmq
 */
@Component
public class TestController {

    public String testAspect(){
        throw new AopConfigException("测试afterThrowing");
    }
}
