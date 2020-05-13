package com.spring.demo.service.impl;

import com.spring.demo.service.IDemoService;
import com.spring.mvcframework.annotaion.ZbyService;

/**
 * 核心业务逻辑
 */
@ZbyService
public class DemoService implements IDemoService {

    @Override
    public String get(String name) {
        return "My name is " + name + ",from service.";
    }

}
