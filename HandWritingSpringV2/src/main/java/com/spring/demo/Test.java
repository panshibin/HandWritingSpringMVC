package com.spring.demo;

import com.spring.mvcframework.annotaion.ZbyService;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/01 09:33
 */
public class Test {
    public static void main(String[] args) {
        try {
            Class<?> clazz = Class.forName("com.spring.demo.service.impl.DemoService");
            System.out.println(clazz);
            Method[] methods = clazz.getMethods();
            Class<?>[] interfaces = clazz.getInterfaces();
            System.out.println(Arrays.toString(interfaces));
            System.out.println(methods);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
