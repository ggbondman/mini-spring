package com.zmq.aop;

import org.aspectj.weaver.tools.*;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author zmq
 */
public class AspectjPointcut {

    private final PointcutExpression pointcutExpression;

    private final Class<?> pointcutScope;

    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = Set.of(
            PointcutPrimitive.EXECUTION,
            PointcutPrimitive.ARGS,
            PointcutPrimitive.REFERENCE,
            PointcutPrimitive.THIS,
            PointcutPrimitive.TARGET,
            PointcutPrimitive.WITHIN,
            PointcutPrimitive.AT_ANNOTATION,
            PointcutPrimitive.AT_WITHIN,
            PointcutPrimitive.AT_ARGS,
            PointcutPrimitive.AT_TARGET);

    public AspectjPointcut(String expression, Class<?> pointcutScope) {
        this.pointcutScope = pointcutScope;
        this.pointcutExpression = buildPointcutExpression(expression);

    }

    private PointcutExpression buildPointcutExpression(String expression){
        PointcutParser parser = PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingContextClassloaderForResolution(SUPPORTED_PRIMITIVES);
        return parser.parsePointcutExpression(expression,pointcutScope,new PointcutParameter[0]);
    }

    public boolean matchClass(Class<?> targetClass){
        return this.pointcutExpression.couldMatchJoinPointsInType(targetClass);
    }

    public boolean matchMethod(Method method){
        ShadowMatch shadowMatch = pointcutExpression.matchesMethodExecution(method);
        if (shadowMatch.alwaysMatches()) {
            return true;
        } else if (shadowMatch.neverMatches()) {
            return false;
        }
        return false;
    }

}
