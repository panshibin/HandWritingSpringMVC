package com.spring.demo.mvc.action;


import com.spring.demo.service.IDemoService;
import com.spring.mvcframework.annotaion.ZbyAutowired;
import com.spring.mvcframework.annotaion.ZbyController;
import com.spring.mvcframework.annotaion.ZbyRequestMapping;
import com.spring.mvcframework.annotaion.ZbyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@ZbyController
@ZbyRequestMapping("/demo")
public class DemoAction {

    @ZbyAutowired
    private IDemoService demoService;

    @ZbyRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @ZbyRequestParam("name") String name) {
        String result = demoService.get(name);
//		String result = "My name is " + name;
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ZbyRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @ZbyRequestParam("a") Integer a, @ZbyRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ZbyRequestMapping("/sub")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @ZbyRequestParam("a") Double a, @ZbyRequestParam("b") Double b) {
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @ZbyRequestMapping("/remove")
    public String remove(@ZbyRequestParam("id") Integer id) {
        return "" + id;
    }

}
