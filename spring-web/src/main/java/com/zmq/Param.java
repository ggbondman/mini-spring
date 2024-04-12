package com.zmq;

import com.google.common.base.Strings;
import com.zmq.annotation.PathVariable;
import com.zmq.annotation.RequestBody;
import com.zmq.annotation.RequestParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

/**
 * @author zmq
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Param {

    // http参数名称:
    private String name;
    // http参数类型:
    private ParamType paramType;
    // http参数Class类型:
    private Class<?> classType;

    private Type genericParameterType;

    // http参数默认值
    private String defaultValue;

    public Param(Parameter parameter, Annotation annotation) {
        this.classType = parameter.getType();
        this.genericParameterType = parameter.getParameterizedType();
        if (annotation == null) {
            this.paramType = ParamType.SERVLET_VARIABLE;
        } else {
            if (annotation instanceof RequestParam requestParam) {
                this.name = Strings.isNullOrEmpty(requestParam.value()) ? parameter.getName() : requestParam.value();
                this.defaultValue = requestParam.defaultValue();
                this.paramType = ParamType.REQUEST_PARAM;
            }
            if (annotation instanceof RequestBody) {
                this.paramType = ParamType.REQUEST_BODY;
            }
            if (annotation instanceof PathVariable pathVariable) {
                this.name = Strings.isNullOrEmpty(pathVariable.value()) ? parameter.getName() : pathVariable.value();
                this.paramType = ParamType.PATH_VARIABLE;
            }
        }
    }
}
