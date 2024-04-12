package com.zmq.view;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.Map;

/**
 * @author zmq
 */
@Data
public class ModelAndView {
    private String viewPath;

    private Map<String, Object> model;

    public ModelAndView(String viewPath, @Nullable Map<String, Object> model) {
        this.viewPath = viewPath;
        this.model = model;
    }


}
