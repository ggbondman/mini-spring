package com.zmq.test.controller;

import com.zmq.RequestMethod;
import com.zmq.annotation.*;
import com.zmq.test.Student;
import com.zmq.view.ModelAndView;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static java.lang.System.out;

/**
 * @author zmq
 */
@Controller
public class TestController {

    @RequestMapping("/test/void")
    @ResponseBody
    public void test() {
        out.println("hello Bob!");
    }

    @RequestMapping(value = "/test/requestParam", method = RequestMethod.GET)
    @ResponseBody
    public String test1(@RequestParam("id") String id) {
        out.println(id);
        return id;
    }

    @RequestMapping(value = "/test/requestBody", method = RequestMethod.POST)
    @ResponseBody
    public Student test2(@RequestBody Student student) {
        out.println(student);
        return student;
    }

    @RequestMapping(value = "/test/path/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String test3(@PathVariable String id) {
        return id;
    }

    @RequestMapping(value = "/test/view", method = RequestMethod.GET)
    public ModelAndView testView() {
        return new ModelAndView("index.html", null);
    }

    @RequestMapping(value = "/test/servlet/{path}", method = RequestMethod.PUT)
    @ResponseBody
    public void testServletVariable(@RequestParam Integer id, @PathVariable String path, @RequestBody Student student, HttpServletRequest request, HttpServletResponse resp, HttpSession session, ServletContext servletContext) {
        out.println(id);
        out.println(path);
        out.println(student);
        out.println(request);
        out.println(resp);
        out.println(session);
        out.println(servletContext);

    }


}
