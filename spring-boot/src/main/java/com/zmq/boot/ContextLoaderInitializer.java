package com.zmq.boot;

import com.zmq.config.WebMvcConfiguration;
import com.zmq.context.AnnotationConfigApplicationContext;
import com.zmq.context.ApplicationContext;
import com.zmq.property.PropertyResolver;
import com.zmq.utils.WebUtils;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;

import java.util.Set;

/**
 * @author zmq
 */
public class ContextLoaderInitializer implements ServletContainerInitializer {

    final Class<?> configClass;
    final PropertyResolver propertyResolver;

    public ContextLoaderInitializer(Class<?> configClass, PropertyResolver propertyResolver) {
        this.configClass = configClass;
        this.propertyResolver = propertyResolver;
    }
    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
        // 设置ServletContext:
        WebMvcConfiguration.setServletContext(ctx);
        // 启动IoC容器:
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(this.configClass, this.propertyResolver);
        // 注册Filter与DispatcherServlet:
        WebUtils.registerFilters(ctx);
        WebUtils.registerDispatcherServlet(ctx, this.propertyResolver);
    }
}
