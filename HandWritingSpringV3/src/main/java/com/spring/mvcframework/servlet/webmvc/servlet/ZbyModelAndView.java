package com.spring.mvcframework.servlet.webmvc.servlet;

import java.util.Map;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/17 11:33
 */
public class ZbyModelAndView {
    private String viewName;//要返回的页面
    private Map<String,?> model;

    public ZbyModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public ZbyModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }
}
