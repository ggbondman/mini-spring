package com.zmq;

import com.zmq.exception.BadRequestException;
import com.zmq.view.ModelAndView;
import com.zmq.view.ViewResolver;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zmq
 */
@Data
public class Result {

    private static final int OK = 200;
    private static final int BAD_REQUEST = 400;
    private static final int SEVER_ERROR = 500;

    private int code;
    private Map<String,String> headers = new HashMap<>();
    private Object content;

    public Result(Object content) {
        this.code = OK;
        this.content = content;
    }

    public Result(Throwable e) {
        if (e instanceof BadRequestException) {
            this.code = BAD_REQUEST;
        } else {
            this.code = SEVER_ERROR;
        }
        this.content = e.getMessage();
    }

    public void send(HttpServletRequest request, HttpServletResponse response, @Nullable ViewResolver viewResolver) throws IOException {
        if (this.code!=OK){
            response.sendError(this.code);
        }
        response.setStatus(this.code);
        this.headers.forEach(response::addHeader);
        PrintWriter writer = response.getWriter();
        if (this.content==null){
            writer.flush();
        }
        if (this.content instanceof ModelAndView modelAndView){
            viewResolver.render(modelAndView.getViewPath(), modelAndView.getModel(), request, response);
        } else{
            response.getWriter().write(this.content.toString());
            writer.flush();
        }
    }
}
