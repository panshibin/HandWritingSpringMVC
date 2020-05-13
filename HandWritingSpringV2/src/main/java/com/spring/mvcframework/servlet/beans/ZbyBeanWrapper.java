package com.spring.mvcframework.servlet.beans;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/11 18:17
 */
public class ZbyBeanWrapper {
    private Object wrappInstance;
    private Class<?> wrappedClass;

    public ZbyBeanWrapper(Object instance) {
         this.wrappInstance = instance;
         this.wrappedClass = instance.getClass();
    }

    public Object getWrappInstance() {
        return wrappInstance;
    }

    public ZbyBeanWrapper setWrappInstance(Object wrappInstance) {
        this.wrappInstance = wrappInstance;
        return this;
    }

    public Class<?> getWrappedClass() {
        return wrappedClass;
    }

    public ZbyBeanWrapper setWrappedClass(Class<?> wrappedClass) {
        this.wrappedClass = wrappedClass;
        return this;
    }
}
