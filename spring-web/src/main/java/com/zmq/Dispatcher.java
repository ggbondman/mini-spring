package com.zmq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.zmq.annotation.*;
import com.zmq.exception.ControllerParameterException;
import com.zmq.exception.ServerErrorException;
import com.zmq.utils.AntPathMatcher;
import com.zmq.utils.ClassUtils;
import com.zmq.utils.PathMatcher;
import com.zmq.view.ModelAndView;
import jakarta.annotation.Nullable;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Data;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zmq
 */
@Data
public class Dispatcher {

    private final static Map<Class<?>, Function<String, Object>> PARAMETER_CONVERTER = new HashMap<>(ClassUtils.CLASS_CONVERTER);

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 可匹配的请求方法:
    private RequestMethod[] requestMethods;
    // 是否有@ResponseBody:
    private boolean isResponseBody;
    // 是否返回void:
    private boolean returnVoid;
    // URL匹配:
    private String urlPattern;
    // Bean实例:
    private Object controller;
    // 处理方法:
    private Method handlerMethod;
    // 方法参数:
    private Param[] methodParameters;

    public Dispatcher(RequestMethod[] requestMethods, boolean isResponseBody, boolean returnVoid, String urlPattern, Object controller, Method handlerMethod, Param[] methodParameters) {
        this.requestMethods = requestMethods;
        this.isResponseBody = isResponseBody;
        this.returnVoid = returnVoid;
        this.urlPattern = urlPattern;
        this.controller = controller;
        this.handlerMethod = handlerMethod;
        this.methodParameters = methodParameters;
    }

    public Dispatcher(Method method, Object controller, boolean isResponseBody) throws ServletException {
        if (method.isAnnotationPresent(ResponseBody.class) || isResponseBody) {
            this.isResponseBody = true;
        }
        if (method.getReturnType() == Void.class) {
            this.returnVoid = true;
        }
        this.handlerMethod = method;
        Parameter[] parameters = method.getParameters();
        this.methodParameters = new Param[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            this.methodParameters[i] = getParam(method, parameters[i]);
        }
        this.controller = controller;
        resolveRequestMapping(method);
    }

    private void resolveRequestMapping(Method method) {
        RequestMapping mapping = method.getAnnotation(RequestMapping.class);
        Class<?> declaringClass = method.getDeclaringClass();
        PathMatcher pathMatcher = new AntPathMatcher();
        if (declaringClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping parentMapping = declaringClass.getAnnotation(RequestMapping.class);
            String parentPattern = parentMapping.value();
            this.urlPattern = pathMatcher.combine(parentPattern, mapping.value());
        } else {
            this.urlPattern = mapping.value();
        }
        this.requestMethods = mapping.method().length==0?RequestMethod.values():mapping.method();
    }

    private Param getParam(Method method, Parameter parameter) throws ServletException {
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        RequestBody requestBody = parameter.getAnnotation(RequestBody.class);
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        int totalAnno = (requestParam == null ? 0 : 1) + (requestBody == null ? 0 : 1) + (pathVariable == null ? 0 : 1);
        if (totalAnno > 1) {
            throw new ServletException("Annotation @PathVariable, @RequestParam and @RequestBody cannot be combined at method: " + method);
        }
        Class<?> classType = parameter.getType();
        // check servlet variable type:
        if (totalAnno == 0 && classType != HttpServletRequest.class && classType != HttpServletResponse.class && classType != HttpSession.class
                && classType != ServletContext.class) {
            throw new ServerErrorException("(Missing annotation?) Unsupported argument type: " + classType + " at method: " + method);
        }
        if (requestParam != null) {
            return new Param(parameter, requestParam);
        }
        if (requestBody != null) {
            return new Param(parameter, requestBody);
        }
        if (pathVariable != null) {
            return new Param(parameter, pathVariable);
        }
        return new Param(parameter, null);
    }

    public Result proceed(HttpServletRequest req, HttpServletResponse resp) {
        try {
            return new Result(doProceed(req, resp));
        } catch (Throwable e) {
            e.printStackTrace();
            return new Result(e);
        }
    }

    private Object doProceed(HttpServletRequest req, HttpServletResponse resp) {
        Object[] args = new Object[this.methodParameters.length];
        Param[] parameters = this.methodParameters;
        for (int i = 0; i < parameters.length; i++) {
            Param param = parameters[i];
            args[i] = switch (param.getParamType()) {
                case ParamType.REQUEST_PARAM -> bindRequestParameter(req, param, i);
                case ParamType.PATH_VARIABLE -> bindPathParameter(req, param, i);
                case ParamType.REQUEST_BODY -> bindRequestBody(req, param, i);
                case ParamType.SERVLET_VARIABLE -> bindServletVariable(req, resp, param, i);
            };
        }
        Object result;
        try {
            result = this.handlerMethod.invoke(this.controller, args);
        } catch (ReflectiveOperationException e) {
            throw new ServerErrorException("A failure has been happened in invoking "+this.handlerMethod.toGenericString(),e);
        }
        if (this.returnVoid){
            return null;
        }else if (this.isResponseBody) {
            try {
                result = OBJECT_MAPPER.writeValueAsString(result);
            } catch (JsonProcessingException e) {
                throw new ServerErrorException("A failure has been happened in invoking "+this.handlerMethod.toGenericString()+": "+e);
            }
        }else if (!(result instanceof ModelAndView)) {
            throw new ServerErrorException("A failure has been happened in invoking "+this.handlerMethod.toGenericString()+": Needed ModelAndView has not returned.");
        }
        return result;
    }

    private Object bindServletVariable(HttpServletRequest req, HttpServletResponse resp, Param param, int paramIndex) {
        Class<?> classType = param.getClassType();
        if (classType == HttpServletRequest.class) {
            return req;
        } else if (classType == HttpServletResponse.class) {
            return resp;
        } else if (classType == HttpSession.class) {
            return req.getSession();
        } else if (classType == ServletContext.class) {
            return req.getServletContext();
        } else {
            throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in "+this.handlerMethod.toGenericString()+": Could not determine argument type: "+classType);
        }
    }

    private Object bindRequestBody(HttpServletRequest req, Param param, int paramIndex) {
        String json;
        try {
            json = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in "+this.handlerMethod.toGenericString()+": Required request body is missing:"+this.handlerMethod.toGenericString());

        }
        Object result;
        try {
            result = OBJECT_MAPPER.readValue(json, param.getClassType());
        } catch (JsonProcessingException e) {
            throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in "+this.handlerMethod.toGenericString()+": JSON parse error: JSON parse error: "+e);
        }
        return result;
    }

    private Object bindPathParameter(HttpServletRequest req, Param param, int paramIndex) {
        String uri = req.getRequestURI();
        Map<String, String> pathParamterMap = PathMatcher.ANT_PATH_MATCHER.extractUriTemplateVariables(this.urlPattern, uri);
        if (pathParamterMap.isEmpty()) {
            throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in public "+this.handlerMethod.toGenericString()+": Required URI template variable '"+param.getName()+"' for method parameter type "+param.getClassType().getSimpleName()+" is not present");
        }
        String pathParam = pathParamterMap.get(param.getName());
        if (!PARAMETER_CONVERTER.containsKey(param.getClassType())) {
            throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in public "+this.handlerMethod.toGenericString()+": Failed to convert value of type 'java.lang.String' to required type '"+param.getClassType()+"'; For input string: \""+pathParam+"\"");
        }
        return PARAMETER_CONVERTER.get(param.getClassType()).apply(pathParam);
    }

    private Object bindRequestParameter(HttpServletRequest req, Param param, int paramIndex) {
        Map<String, String[]> parameterMap = req.getParameterMap();
        String[] values = parameterMap.get(param.getName());
        if (values.length==0){
            throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in public "+this.handlerMethod.toGenericString()+": The parameter values has not found");
        }
        Class<?> classType = param.getClassType();
        if (Collections.class.isAssignableFrom(classType)) {
            Type parameterType = param.getGenericParameterType();
            if (parameterType instanceof ParameterizedType parameterizedType) {
                Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                if (typeArgument instanceof Class<?> typeClass) {
                    Collection<Object> collection = createCollection(classType, typeClass, values.length);
                    for (String value : values) {
                        if (PARAMETER_CONVERTER.containsKey(typeClass)) {
                            collection.add(PARAMETER_CONVERTER.get(typeClass).apply(value));
                        }
                    }
                    return collection;
                } else if (typeArgument instanceof ParameterizedType) {
                    throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in public "+this.handlerMethod.toGenericString()+": Nested Collection and Map are not supported for the current version");
                }
            }
        } else if (classType.isArray()) {
            Class<?> componentType = classType.componentType();
            Object[] arr = new Object[values.length];
            for (int j = 0; j < values.length; j++) {
                if (PARAMETER_CONVERTER.containsKey(componentType)) {
                    arr[j] = PARAMETER_CONVERTER.get(componentType).apply(values[j]);
                }
            }
            return arr;
        } else if (classType == String.class) {
            return Joiner.on(",").join(values);
        } else if (PARAMETER_CONVERTER.containsKey(classType)) {
            return PARAMETER_CONVERTER.get(classType).apply(values[0]);
        }
        throw new ControllerParameterException("Could not resolve parameter ["+paramIndex+"] in public "+this.handlerMethod.toGenericString()+": Failed to convert value of type 'java.lang.String' to required type '"+classType+"'; For input string: \""+values[0]+"\"");
    }

    @SuppressWarnings("unchecked")
    public <E> Collection<E> createCollection(Class<?> collectionType, @Nullable Class<?> elementType, int capacity) {
        if (LinkedHashSet.class == collectionType ||
                Set.class == collectionType || Collection.class == collectionType) {
            return new LinkedHashSet<>(capacity);
        } else if (ArrayList.class == collectionType || List.class == collectionType) {
            return new ArrayList<>(capacity);
        } else if (LinkedList.class == collectionType) {
            return new LinkedList<>();
        } else if (TreeSet.class == collectionType || NavigableSet.class == collectionType ||
                SortedSet.class == collectionType) {
            return new TreeSet<>();
        } else if (EnumSet.class.isAssignableFrom(collectionType)) {
            return (Collection<E>) EnumSet.noneOf(elementType.asSubclass(Enum.class));
        } else if (HashSet.class == collectionType) {
            return new HashSet<>(capacity);
        } else {
            if (collectionType.isInterface() || !Collection.class.isAssignableFrom(collectionType)) {
                throw new IllegalArgumentException("Unsupported Collection type: " + collectionType.getName());
            }
            try {
                Constructor<?> constructor = collectionType.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (Collection<E>) constructor.newInstance();
            } catch (Throwable ex) {
                throw new IllegalArgumentException(
                        "Could not instantiate Collection type: " + collectionType.getName(), ex);
            }
        }
    }
}