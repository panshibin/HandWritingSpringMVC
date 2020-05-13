package com.spring.demo.mvc.action;


import com.spring.demo.service.IDemoService;
import com.spring.mvcframework.annotaion.ZbyAutowired;
import com.spring.mvcframework.annotaion.ZbyController;
import com.spring.mvcframework.annotaion.ZbyRequestMapping;
import com.spring.mvcframework.annotaion.ZbyRequestParam;
import com.spring.mvcframework.servlet.webmvc.servlet.ZbyModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@ZbyController
@ZbyRequestMapping("/demo")
public class DemoAction {

    @ZbyAutowired
    private IDemoService demoService;

    @ZbyRequestMapping("/query")
    public ZbyModelAndView query(HttpServletRequest req, HttpServletResponse resp,
                      @ZbyRequestParam("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ZbyRequestMapping("/first.html")
    public ZbyModelAndView query(@ZbyRequestParam("teacher") String teacher) {
        String result = demoService.get(teacher);
        Map<String, Object> map = new HashMap<>();
        map.put("teacher", teacher);
        map.put("data", result);
        map.put("token", 123123);
        return new ZbyModelAndView("first.html", map);
    }

    @ZbyRequestMapping("/add*.json")
    public ZbyModelAndView add(HttpServletRequest req, HttpServletResponse resp,
                    @ZbyRequestParam("a") Integer a, @ZbyRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ZbyRequestMapping("/sub")
    public ZbyModelAndView add(HttpServletRequest req, HttpServletResponse resp,
                    @ZbyRequestParam("a") Double a, @ZbyRequestParam("b") Double b) {
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @ZbyRequestMapping("/remove")
    public ZbyModelAndView remove(@ZbyRequestParam("id") Integer id) {
        return null;
    }

}
