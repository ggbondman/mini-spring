package com.zmq.view;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * @author zmq
 */
public interface ViewResolver {
    // 初始化ViewResolver:
    void init();

    // 渲染:
    void render(String viewName, Map<String, Object> model, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
