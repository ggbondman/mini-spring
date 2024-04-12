package com.zmq.aop;


import jakarta.annotation.Nullable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

/**
 * @author zmq
 */
public class MethodInvocationProceedingJoinPoint implements ProceedingJoinPoint {

    private final ProxyMethodInvocation methodInvocation;

    private Signature signature;

    public MethodInvocationProceedingJoinPoint(ProxyMethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    @Override
    public void set$AroundClosure(AroundClosure arc) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object proceed() throws Throwable {
        return this.methodInvocation.proceed();
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        this.methodInvocation.setArguments(args);
        return this.methodInvocation.proceed();
    }


    @Override
    public String toShortString() {
        return STR."execution(\{getSignature().toShortString()})";
    }

    @Override
    public String toLongString() {
        return null;
    }

    @Override
    public Object getThis() {
        return this.methodInvocation.getProxy();
    }

    @Override
    public Object getTarget() {
        return this.methodInvocation.getThis();
    }

    @Override
    public Object[] getArgs() {
        return this.methodInvocation.getArguments();
    }

    @Override
    public Signature getSignature() {
        return this.signature==null?new MethodSignatureImpl():this.signature;
    }

    @Override
    public SourceLocation getSourceLocation() {
        return null;
    }

    @Override
    public String getKind() {
        return null;
    }

    @Override
    public StaticPart getStaticPart() {
        return null;
    }

    private class MethodSignatureImpl implements MethodSignature {

        @Nullable
        private String[] parameterNames;


        @Override
        public Class<?> getReturnType() {
            return getMethod().getReturnType();
        }

        @Override
        public Method getMethod() {
            return methodInvocation.getMethod();
        }

        @Override
        public Class<?>[] getParameterTypes() {
            return getMethod().getParameterTypes();
        }

        @Override
        public String[] getParameterNames() {
            String[] parameterNames = this.parameterNames;
            if (parameterNames==null) {
                Parameter[] parameters = getMethod().getParameters();
                parameterNames = new String[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    if (!parameters[i].isNamePresent()){
                        return null;
                    }
                    parameterNames[i] = parameters[i].getName();
                }
            }
            return parameterNames;
        }

        @Override
        public Class<?>[] getExceptionTypes() {
            return getMethod().getExceptionTypes();
        }

        @Override
        public String toShortString() {
            return toString(false, false, false, false);
        }

        @Override
        public String toLongString() {
            return toString(true, true, true, true);
        }


        @Override
        public String getName() {
            return getMethod().getName();
        }

        @Override
        public int getModifiers() {
            return getMethod().getModifiers();
        }

        @Override
        public Class<?> getDeclaringType() {
            return getMethod().getDeclaringClass();
        }

        @Override
        public String getDeclaringTypeName() {
            return getDeclaringType().getName();
        }

        private String toString(boolean includeModifier, boolean includeReturnTypeAndArgs,
                                boolean useLongReturnAndArgumentTypeName, boolean useLongTypeName) {

            StringBuilder sb = new StringBuilder();
            if (includeModifier) {
                sb.append(Modifier.toString(getModifiers()));
                sb.append(' ');
            }
            if (includeReturnTypeAndArgs) {
                appendType(sb, getReturnType(), useLongReturnAndArgumentTypeName);
                sb.append(' ');
            }
            appendType(sb, getDeclaringType(), useLongTypeName);
            sb.append('.');
            sb.append(getMethod().getName());
            sb.append('(');
            Class<?>[] parametersTypes = getParameterTypes();
            appendTypes(sb, parametersTypes, includeReturnTypeAndArgs, useLongReturnAndArgumentTypeName);
            sb.append(')');
            return sb.toString();
        }

        private void appendTypes(StringBuilder sb, Class<?>[] types, boolean includeArgs,
                                 boolean useLongReturnAndArgumentTypeName) {

            if (includeArgs) {
                for (int size = types.length, i = 0; i < size; i++) {
                    appendType(sb, types[i], useLongReturnAndArgumentTypeName);
                    if (i < size - 1) {
                        sb.append(',');
                    }
                }
            }
            else {
                if (types.length != 0) {
                    sb.append("..");
                }
            }
        }

        private void appendType(StringBuilder sb, Class<?> type, boolean useLongTypeName) {
            if (type.isArray()) {
                appendType(sb, type.getComponentType(), useLongTypeName);
                sb.append("[]");
            }
            else {
                sb.append(useLongTypeName ? type.getName() : type.getSimpleName());
            }
        }
    }
}