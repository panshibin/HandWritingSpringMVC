package com.spring.mvcframework.servlet;

import com.spring.demo.mvc.action.TestAction;
import com.spring.mvcframework.servlet.context.ZbyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 使用设计模式：
 *      1、委派模式
 *          职责：负责任务调度，任务分发
 *
 * @author zhaobinyang
 * @date 2020/04/11 16:42
 */
public class ZbyDispathCherServlet extends HttpServlet {

    private ZbyApplicationContext zbyApplicationContext;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、初始化Spring核心IOC容器
        zbyApplicationContext = new ZbyApplicationContext(config.getInitParameter("contextConfigLocation"));

        TestAction testAction = (TestAction)zbyApplicationContext.getBean("testAction");
        testAction.show();

    }
}
