package com.zmq.controller;

import com.zmq.RequestMethod;
import com.zmq.annotation.Autowired;
import com.zmq.annotation.Controller;
import com.zmq.annotation.RequestMapping;
import com.zmq.serive.TestJdbc;
import com.zmq.view.ModelAndView;

/**
 * @author zmq
 */
@Controller
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    private TestJdbc testJdbc;

    @RequestMapping(method = RequestMethod.GET,value = "/")
    public ModelAndView testHtml(){
        return new ModelAndView("/index.html",null);
    }

    @RequestMapping(method = RequestMethod.GET,value = "/transaction")
    public void testTransaction(){
        testJdbc.testTransaction(6,"666");
    }
}
