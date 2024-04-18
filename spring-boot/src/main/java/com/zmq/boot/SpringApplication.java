package com.zmq.boot;

import com.zmq.property.PropertyResolver;
import com.zmq.utils.ClassUtils;
import com.zmq.utils.WebUtils;
import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.util.Objects;
import java.util.Set;

/**
 * @author zmq
 */
public class SpringApplication {


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
        // 设置tomcat的基本目录，在系统默认的临时目录下
        // File baseDir = Files.createTempDirectory("tomcat." + port + ".").toFile();
        // baseDir.deleteOnExit();
        // tomcat.setBaseDir(baseDir.getAbsolutePath());

        Context ctx = tomcat.addWebapp("", Objects.requireNonNull(ClassUtils.getDefaultClassLoader().getResource(".")).getFile());
        // Context ctx = tomcat.addWebapp("", new File("src/test/webapp").getAbsolutePath());
        // 设置应用程序的目录:
        WebResourceRoot resources = new StandardRoot(ctx);
        // 使 Tomcat 在查找资源时首先检查这个目录 target/classes
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(resources);
        // 设置ServletContainerInitializer监听器:
        ctx.addServletContainerInitializer(new ContextLoaderInitializer(configClass, propertyResolver), Set.of());
        // 启动服务器:
        tomcat.start();
        return tomcat.getServer();
    }
}
