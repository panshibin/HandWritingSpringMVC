package com.spring.mvcframework.servlet.context;

import com.spring.mvcframework.annotaion.ZbyAutowired;
import com.spring.mvcframework.annotaion.ZbyController;
import com.spring.mvcframework.annotaion.ZbyRequestMapping;
import com.spring.mvcframework.annotaion.ZbyService;
import com.spring.mvcframework.servlet.beans.support.ZbyBeanDefinitionReader;
import com.spring.mvcframework.servlet.beans.ZbyBeanWrapper;
import com.spring.mvcframework.servlet.beans.config.ZbyBeanDefinition;
import com.spring.mvcframework.servlet.webmvc.servlet.ZbyHandlerMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/11 16:44
 */
public class ZbyApplicationContext {

    private ZbyBeanDefinitionReader reader;

    private Map<String, ZbyBeanDefinition> beanDefinitionMap = new HashMap<String, ZbyBeanDefinition>();
    //Bean的缓存
    private Map<String, ZbyBeanWrapper> factoryBeanInstanceCache = new HashMap<String, ZbyBeanWrapper>();

    //保存原生对象
    private Map<String, Object> factoryBeanObjectCache = new HashMap<String, Object>();

    //配置文件
    public ZbyApplicationContext(String configLocation) {

        //1、加载配置文件
        reader = new ZbyBeanDefinitionReader(configLocation);

        //2、解析配置文件、封装成BeanDefinition集合对象
        List<ZbyBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();


        //3、将扫描的相关类，保存到IOC容器中。把BeanDefinition缓存起来
        try {
            doRegistBeanDefinition(beanDefinitions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //4、完成DI注入
        doAutoWrited();
    }

    /**
     * 如果不是延迟加载则加载
     */
    private void doAutoWrited() {
        //调用getBean()触发
        if (beanDefinitionMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, ZbyBeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }
    }

    private void doRegistBeanDefinition(List<ZbyBeanDefinition> beanDefinitions) throws Exception {
        for (ZbyBeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The" + beanDefinition.getFactoryBeanName() + "is exists!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }
    }

    /**
     * Bean的实例化，DI是从这个方法开始的
     *
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        //1、先拿到BeanDefinition配置信息
        ZbyBeanDefinition zbyBeanDefinition = this.beanDefinitionMap.get(beanName);
        //2、反射实例化newInstance();
        Object instance = instantiateBean(beanName, zbyBeanDefinition);
        //3、封装成一个叫BeanWrapperd对象
        ZbyBeanWrapper zbyBeanWrapper = new ZbyBeanWrapper(instance);
        //4、保存到IOC容器
        factoryBeanInstanceCache.put(beanName, zbyBeanWrapper);
        //5、执行依赖注入
        populateBean(beanName, zbyBeanDefinition, zbyBeanWrapper);

        return zbyBeanWrapper.getWrappInstance();
    }

    private void populateBean(String beanName, ZbyBeanDefinition zbyBeanDefinition, ZbyBeanWrapper zbyBeanWrapper) {

        //如果是循环依赖，则循环两次即可
        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2、等第一次循环之后，第二次循环再检查第一次的缓存，在进行赋值

        Object instance = zbyBeanWrapper.getWrappInstance();

        Class<?> clazz = zbyBeanWrapper.getWrappedClass();

        //在spring中的component
        if (!(clazz.isAnnotationPresent(ZbyController.class) || !clazz.isAnnotationPresent(ZbyService.class))) {
            return;
        }

        //获取所有字段,包含publice、private、protected、default修饰的字段
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(ZbyAutowired.class)) {
                continue;
            }

            ZbyAutowired autowired = field.getAnnotation(ZbyAutowired.class);
            //如果用户没有自定义的beanName，就默认根据类型注入
            String autowiredBeanName = autowired.value().trim();
            if ("".equals(autowiredBeanName)) {
                //获取字段类型
                autowiredBeanName = field.getType().getName();
            }

            //如果存在private类型，则需要暴力访问
            field.setAccessible(true);
            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    return;
                }
                //接口的实现类（相当于通过接口的全名拿到接口的实现的实例），拿到后最后通过set赋值
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object instantiateBean(String beanName, ZbyBeanDefinition zbyBeanDefinition) {
        String className = zbyBeanDefinition.getBeanClassName();
        Object instance = null;
        try {
            if (this.factoryBeanObjectCache.containsKey(beanName)) {
                instance = this.factoryBeanObjectCache.get(beanName);
            } else {
                Class<?> clazz = Class.forName(className);
                //默认的类首字母小写
                instance = clazz.newInstance();
                //存储原生对象
                this.factoryBeanObjectCache.put(beanName, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public int getBeanDefinitionCount(){
        return this.beanDefinitionMap.size();
    }

    public Object getBean(Class beanClazz) {
        return getBean(beanClazz.getName());
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
