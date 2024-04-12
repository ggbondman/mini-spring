package com.zmq.controller;

import com.zmq.annotation.Component;

import static java.lang.System.out;

/**
 * @author zmq
 */
@Component
public class TestController {

    public String testAspect(){
        out.println("测试aspect");
        return "success";
    }
}
