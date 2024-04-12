package com.zmq;

import com.zmq.context.AnnotationConfigApplicationContext;
import com.zmq.context.ApplicationContext;
import com.zmq.property.PropertyResolver;
import com.zmq.utils.WebUtils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import static java.lang.System.out;

/**
 * @author zmq
 */
public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        out.println("开始初始化");
        ServletContext servletContext = sce.getServletContext();
        PropertyResolver propertyResolver = WebUtils.createPropertyResolver();
        String encoding = propertyResolver.getProperty("${spring.web.character-encoding:UTF-8}");
        servletContext.setRequestCharacterEncoding(encoding);
        servletContext.setResponseCharacterEncoding(encoding);
        out.println("开始获取ApplicationContext");
        ApplicationContext applicationContext = createApplicationContext(servletContext.getInitParameter("configuration"), propertyResolver);
        out.println("获取ApplicationContext结束");
        // register DispatcherServlet:
        WebUtils.registerDispatcherServlet(servletContext, propertyResolver);
        servletContext.setAttribute("applicationContext", applicationContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (sce.getServletContext().getAttribute("applicationContext") instanceof ApplicationContext applicationContext) {
            applicationContext.close();
        }
    }

    ApplicationContext createApplicationContext(String configClassName, PropertyResolver propertyResolver) {
        out.println(STR."init ApplicationContext by configuration: \{configClassName}");
        if (configClassName == null || configClassName.isEmpty()) {
            throw new RuntimeException("Cannot init ApplicationContext for missing init param name: configuration");
        }
        Class<?> configClass;
        try {
            configClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not load class from init param 'configuration': " + configClassName);
        }
        return new AnnotationConfigApplicationContext(configClass, propertyResolver);
    }
}
