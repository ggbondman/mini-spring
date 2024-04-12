package com.zmq.boot;

import com.zmq.property.PropertyResolver;
import com.zmq.utils.WebUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.util.Set;

/**
 * @author zmq
 */
public class SpringApplication {

    private static final String BASE_DIR = "target/classes";

    private static final String WEB_DIR = "src/test/webapp";

    public static void run(Class<?> configClass, String... args) throws Exception {

        // 读取application.yml配置:
        PropertyResolver propertyResolver = WebUtils.createPropertyResolver();

        // 创建Tomcat服务器:
        Server server = startTomcat(configClass, propertyResolver);
        // 等待服务器结束:
        server.await();
    }

    private static Server startTomcat(Class<?> configClass, PropertyResolver propertyResolver) throws Exception {
        int port = propertyResolver.getProperty("${server.port:8080}", int.class);
        // 实例化Tomcat Server:
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        // 设置Connector:
        tomcat.getConnector().setThrowOnFailure(true);
        // 添加一个默认的Webapp，挂载在'/':
        Context ctx = tomcat.addWebapp("", new File(WEB_DIR).getAbsolutePath());
        // 设置应用程序的目录:
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", new File(BASE_DIR).getAbsolutePath(), "/"));
        ctx.setResources(resources);
        // 设置ServletContainerInitializer监听器:
        ctx.addServletContainerInitializer(new ContextLoaderInitializer(configClass, propertyResolver), Set.of());
        // 启动服务器:
        tomcat.start();
        return tomcat.getServer();
    }
}
