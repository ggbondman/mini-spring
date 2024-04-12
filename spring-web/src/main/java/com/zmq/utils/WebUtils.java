package com.zmq.utils;

import com.zmq.DispatcherServlet;
import com.zmq.FilterRegistrationBean;
import com.zmq.context.ApplicationContext;
import com.zmq.context.ApplicationContextUtils;
import com.zmq.io.InputStreamCallback;
import com.zmq.property.PropertyResolver;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.*;

import static java.lang.Boolean.TRUE;
import static java.lang.System.out;

/**
 * @author zmq
 */
public class WebUtils {

    static final String CONFIG_APP_YAML = "/application.yml";
    static final String CONFIG_APP_PROP = "/application.properties";

    public static void registerDispatcherServlet(ServletContext servletContext, PropertyResolver propertyResolver) {
        DispatcherServlet dispatcherServlet = new DispatcherServlet(ApplicationContextUtils.getRequiredApplicationContext(), propertyResolver);
        out.println(STR."register servlet \{dispatcherServlet.getClass().getName()} for URL '/'");
        ServletRegistration.Dynamic dispatcherReg = servletContext.addServlet("dispatcherServlet", dispatcherServlet);
        dispatcherReg.addMapping("/");
        dispatcherReg.setLoadOnStartup(0);
    }

    public static void registerFilters(ServletContext servletContext) {
        ApplicationContext applicationContext = ApplicationContextUtils.getRequiredApplicationContext();
        for (FilterRegistrationBean filterRegBean : applicationContext.getBeans(FilterRegistrationBean.class)) {
            List<String> urlPatterns = filterRegBean.getUrlPatterns();
            if (urlPatterns == null || urlPatterns.isEmpty()) {
                throw new IllegalArgumentException(STR."No url patterns for \{filterRegBean.getClass().getName()}");
            }
            var filter = Objects.requireNonNull(filterRegBean.getFilter(), "FilterRegistrationBean.getFilter() must not return null.");
            out.println(STR."register filter '\{filterRegBean.getName()}' \{filter.getClass().getName()} for URLs: \{String.join(", ", urlPatterns)}");
            var filterReg = servletContext.addFilter(filterRegBean.getName(), filter);
            filterReg.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, urlPatterns.toArray(String[]::new));
        }
    }

    /**
     * Try load property resolver from /application.yml or /application.properties.
     */
    public static PropertyResolver createPropertyResolver() {
        final Properties props = new Properties();
        // try load application.yml:
        try {
            Map<String, Object> ymlMap = YamlUtils.loadYamlAsPlainMap(CONFIG_APP_YAML);
            out.println(STR."load config: \{CONFIG_APP_YAML}");
            for (String key : ymlMap.keySet()) {
                Object value = ymlMap.get(key);
                if (value instanceof String strValue) {
                    props.put(key, strValue);
                }
            }
        } catch (UncheckedIOException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                // try load application.properties:
                ClassPathUtils.readInputStream(CONFIG_APP_PROP, new InputStreamCallback() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <T> T get(InputStream stream) throws IOException {
                        out.println(STR."load config: \{CONFIG_APP_PROP}");
                        props.load(stream);
                        return (T) TRUE;
                    }
                });


            }
        }
        return new PropertyResolver(props);
    }
}
