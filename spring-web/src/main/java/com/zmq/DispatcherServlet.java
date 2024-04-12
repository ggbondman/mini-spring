package com.zmq;

import com.zmq.annotation.Controller;
import com.zmq.annotation.RequestMapping;
import com.zmq.annotation.RestController;
import com.zmq.beans.BeanDefinition;
import com.zmq.beans.ClassMetaData;
import com.zmq.beans.DefaultClassMetaData;
import com.zmq.context.ApplicationContext;
import com.zmq.property.PropertyResolver;
import com.zmq.utils.AntPathMatcher;
import com.zmq.utils.PathMatcher;
import com.zmq.view.ViewResolver;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zmq
 */
@WebServlet(urlPatterns = "/")
public class DispatcherServlet extends HttpServlet {

    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    private ViewResolver viewResolver;

    private String resourcePath;

    private String faviconPath;


    Map<String, Dispatcher> dispatcherMap = new ConcurrentHashMap<>();


    private final ApplicationContext applicationContext;

    private final PropertyResolver propertyResolver;


    public DispatcherServlet(ApplicationContext applicationContext, PropertyResolver propertyResolver) {
        this.applicationContext = applicationContext;
        this.viewResolver = applicationContext.getBean(ViewResolver.class);
        this.propertyResolver = propertyResolver;
        this.resourcePath = propertyResolver.getProperty("${spring.web.static-path:/static/}");
        this.faviconPath = propertyResolver.getProperty("${spring.web.favicon-path:/favicon.ico}");
    }


    @Override
    public void init() throws ServletException {
        for (BeanDefinition def : applicationContext.getAllBeanDefinitions()) {
            Class<?> beanClass = def.getInstance().getClass();
            ClassMetaData classMetaData = new DefaultClassMetaData(beanClass);
            Controller controller = classMetaData.getAnnotation(Controller.class);
            RestController restController = classMetaData.getAnnotation(RestController.class);
            boolean isResponseBody;
            if (controller == null && restController == null) {
                continue;
            }
            if (controller != null && restController != null) {
                throw new ServletException("Found @Controller and @RestController on class: "+beanClass.getName());
            }
            isResponseBody = controller == null;
            for (Method method : classMetaData.getMethodsByAnnotation(RequestMapping.class)) {
                Dispatcher dispatcher = new Dispatcher(method, def.getInstance(), isResponseBody);
                dispatcherMap.put(dispatcher.getUrlPattern(), dispatcher);
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String requestMethod = req.getMethod();
        String url = req.getRequestURI();
        if (RequestMethod.valueOf(requestMethod)==RequestMethod.GET && (url.equals(this.faviconPath) || url.startsWith(this.resourcePath))){
            doResource(url,req,resp);
        }else {
            Dispatcher dispatcher = getSuitableDispatcher(url, requestMethod);
            if (dispatcher != null) {
                Result result = dispatcher.proceed(req, resp);
                result.send(req, resp, this.viewResolver);
            } else {
                resp.sendError(404);
            }
        }
    }

    private void doResource(String url, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ServletContext ctx = req.getServletContext();
        try (InputStream input = this.getClass().getResourceAsStream(url)) {
            if (input == null) {
                resp.sendError(404, "Not Found");
            } else {
                // guess content type:
                String file = url;
                int n = url.lastIndexOf('/');
                if (n >= 0) {
                    file = url.substring(n + 1);
                }
                String mime = ctx.getMimeType(file);
                if (mime == null) {
                    mime = "application/octet-stream";
                }
                resp.setContentType(mime);
                ServletOutputStream output = resp.getOutputStream();
                input.transferTo(output);
                output.flush();
            }
        }
    }

    private Dispatcher getSuitableDispatcher(String url, String requestMethodString) {
        RequestMethod requestMethod = RequestMethod.valueOf(requestMethodString);
        Comparator<String> patternComparator = PATH_MATCHER.getPatternComparator(url);
        TreeSet<String> candidatePattern = new TreeSet<>(patternComparator);
        for (Map.Entry<String, Dispatcher> entry : dispatcherMap.entrySet()) {
            if (PATH_MATCHER.match(entry.getKey(), url) && Arrays.stream(entry.getValue().getRequestMethods()).anyMatch(rm -> rm == requestMethod)) {
                candidatePattern.add(entry.getKey());
            }
        }
        if (candidatePattern.isEmpty()) {
            return null;
        }
        return dispatcherMap.get(candidatePattern.first());
    }

    @Override
    public void destroy() {
        this.applicationContext.close();
    }

}
