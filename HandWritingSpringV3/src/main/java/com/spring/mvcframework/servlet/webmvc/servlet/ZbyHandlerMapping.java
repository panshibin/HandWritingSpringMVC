package com.spring.mvcframework.servlet.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/16 20:46
 */
public class ZbyHandlerMapping {
    private Pattern pattern;//URL
    private Method method;//对应的Method
    private Object controller;//Method对应的实例对象

    public ZbyHandlerMapping(Pattern url, Object controller, Method method) {
        this.pattern = url;
        this.method = method;
        this.controller = controller;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public ZbyHandlerMapping setPattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public Method getMethod() {
        return method;
    }

    public ZbyHandlerMapping setMethod(Method method) {
        this.method = method;
        return this;
    }

    public Object getController() {
        return controller;
    }

    public ZbyHandlerMapping setController(Object controller) {
        this.controller = controller;
        return this;
    }
}
