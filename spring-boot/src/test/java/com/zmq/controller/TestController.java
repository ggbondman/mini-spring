package com.zmq.controller;

import com.zmq.RequestMethod;
import com.zmq.annotation.RequestMapping;
import com.zmq.annotation.RestController;

/**
 * @author zmq
 */
@RestController
public class TestController {
    @RequestMapping(method = RequestMethod.GET,value = "/")
    public String testBootstrap(){
        return "success";
    }

    @RequestMapping(method = RequestMethod.GET,value = "/test")
    public String testBootstrap1(){
        return "success1";
    }
}
