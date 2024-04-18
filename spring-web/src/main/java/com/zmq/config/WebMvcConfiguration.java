package com.zmq.config;

import com.zmq.annotation.Autowired;
import com.zmq.annotation.Bean;
import com.zmq.annotation.Configuration;
import com.zmq.annotation.Value;
import com.zmq.view.FreeMarkerViewResolver;
import jakarta.servlet.ServletContext;

import java.util.Objects;

/**
 * @author zmq
 */
@Configuration
public class WebMvcConfiguration {
    private static ServletContext servletContext = null;

    public static void setServletContext(ServletContext ctx) {
        servletContext = ctx;
    }

    @Bean
    public FreeMarkerViewResolver viewResolver(
            @Autowired ServletContext servletContext,
            @Value("${spring.web.freemarker.template-path:/templates}") String templatePath,
            @Value("${spring.web.freemarker.template-encoding:UTF-8}") String templateEncoding) {
        return new FreeMarkerViewResolver(templatePath, templateEncoding, servletContext);
    }

    @Bean
    public ServletContext servletContext() {
        return Objects.requireNonNull(servletContext, "ServletContext is not set.");
    }

}
