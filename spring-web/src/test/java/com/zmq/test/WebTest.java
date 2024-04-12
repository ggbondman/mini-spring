package com.zmq.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import com.zmq.ControllerConfiguration;
import com.zmq.DispatcherServlet;
import com.zmq.config.WebMvcConfiguration;
import com.zmq.context.AnnotationConfigApplicationContext;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.zmq.utils.WebUtils.createPropertyResolver;
import static java.lang.System.out;

/**
 * @author zmq
 */
public class WebTest {


    DispatcherServlet dispatcherServlet;
    MockServletContext ctx;

    @Test
    void testServlet() throws ServletException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("id", "12345");
        //testPathVariable("GET", "/test/path/1123");
        //testRequestBody("POST", "/test/requestBody", new Student(13, "zmq"), params);
        //testRequestParam("GET", "/test/requestParam", params);
        //testView("GET", "/test/view", params);
        //testResource("GET", "/static/makima.png");
        testAllParams("PUT", "/test/servlet/path?id=99", new Student(44, "spring"), params);
    }

    void testAllParams(String method, String url, Object body, Map<String, Object> params) throws IOException, ServletException {
        MockHttpServletRequest request = createMockRequest(method, url, body, params);
        MockHttpServletResponse response = createMockResponse();
        this.dispatcherServlet.service(request, response);
        Assertions.assertEquals(response.getStatus(), 200);
        printResponse(response);
    }

    private void testResource(String method, String url) throws IOException, ServletException {
        MockHttpServletRequest request = createMockRequest(method, url, null, null);
        MockHttpServletResponse response = createMockResponse();
        this.dispatcherServlet.service(request, response);
        Assertions.assertEquals(response.getStatus(), 200);
        printResponse(response);
    }

    void testView(String method, String url, Map<String, Object> param) throws IOException, ServletException {
        MockHttpServletRequest request = createMockRequest(method, url, null, param);
        MockHttpServletResponse response = createMockResponse();
        this.dispatcherServlet.service(request, response);
        Assertions.assertEquals(response.getStatus(), 200);
        printResponse(response);
    }

    void testRequestParam(String method, String url, Map<String, Object> param) throws IOException, ServletException {
        MockHttpServletRequest request = createMockRequest(method, url, null, param);
        MockHttpServletResponse response = createMockResponse();
        this.dispatcherServlet.service(request, response);
        Assertions.assertEquals(response.getStatus(), 200);
        printResponse(response);
    }

    void testRequestBody(String method, String url, Object body, Map<String, Object> param) throws IOException, ServletException {
        MockHttpServletRequest request = createMockRequest(method, url, body, param);
        MockHttpServletResponse response = createMockResponse();
        this.dispatcherServlet.service(request, response);
        Assertions.assertEquals(response.getStatus(), 200);
        printResponse(response);
    }

    void testPathVariable(String method, String url) throws IOException, ServletException {
        MockHttpServletRequest request = createMockRequest(method, url, null, null);
        MockHttpServletResponse response = createMockResponse();
        this.dispatcherServlet.service(request, response);
        Assertions.assertEquals(response.getStatus(), 200);
        printResponse(response);
    }

    void printResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
        out.println(STR."statusCode: \{response.getStatus()}");
        out.println(STR."content: \{response.getContentAsString()}");
        out.println();

    }

    @BeforeEach
    void init() throws ServletException {
        this.ctx = createMockServletContext();
        WebMvcConfiguration.setServletContext(this.ctx);
        var propertyResolver = createPropertyResolver();
        var applicationContext = new AnnotationConfigApplicationContext(ControllerConfiguration.class, propertyResolver);
        this.dispatcherServlet = new DispatcherServlet(applicationContext, propertyResolver);
        this.dispatcherServlet.init();
    }

    MockServletContext createMockServletContext() {
        Path path = Path.of("./src/test/resources").toAbsolutePath().normalize();
        var ctx = new MockServletContext(STR."file://\{path.toString()}");
        ctx.setRequestCharacterEncoding("UTF-8");
        ctx.setResponseCharacterEncoding("UTF-8");
        return ctx;
    }

    MockHttpServletRequest createMockRequest(String method, String path, Object body, Map<String, Object> params) throws JsonProcessingException {
        int queryIndex = path.indexOf('?');
        String queryString = null;
        if (queryIndex != -1) {
            queryString = path.substring(queryIndex + 1);
            path = path.substring(0, queryIndex);
        }
        var req = new MockHttpServletRequest(this.ctx);

        req.setMethod(method);
        req.setRequestURI(path);
        req.setQueryString(queryString);
        if (queryString != null) {
            for (String s : Splitter.on('&').split(queryString)) {
                Iterator<String> iterator = Splitter.on('=').split(s).iterator();
                req.addParameter(iterator.next(), iterator.next());
            }
        }
        if (body != null) {
            req.setContentType("application/json");
            req.setContent(new ObjectMapper().writeValueAsBytes(body));
        } else {
            req.setContentType("application/x-www-form-urlencoded");
            if (params != null) {
                req.addParameters(params);
            }
        }
        var session = new MockHttpSession();
        req.setSession(session);
        return req;
    }

    MockHttpServletResponse createMockResponse() {
        var resp = new MockHttpServletResponse();
        resp.setDefaultCharacterEncoding("UTF-8");
        return resp;
    }
}
