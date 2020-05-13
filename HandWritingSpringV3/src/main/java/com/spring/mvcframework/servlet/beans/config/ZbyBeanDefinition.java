package com.spring.mvcframework.servlet.beans.config;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/11 16:57
 */
public class ZbyBeanDefinition {
    private String factoryBeanName;
    private String BeanClassName;

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public ZbyBeanDefinition setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
        return this;
    }

    public String getBeanClassName() {
        return BeanClassName;
    }

    public ZbyBeanDefinition setBeanClassName(String beanClassName) {
        BeanClassName = beanClassName;
        return this;
    }
}
