package com.spring.mvcframework.servlet.beans.support;

import com.spring.mvcframework.servlet.beans.config.ZbyBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 负责读取和解析
 *
 * @author zhaobinyang
 * @date 2020/04/11 17:01
 */
public class ZbyBeanDefinitionReader {
    private Properties contextConfig = new Properties();
    //享元模式，保存扫描的结果
    private List<String> regitryBeanClasses = new ArrayList<>();

    public ZbyBeanDefinitionReader(String... configLocations) {
        //1、加载配置文件信息
        doLoadConfig(configLocations[0]);

        //2、扫描配置文件中相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    public Properties getConfig(){
        return this.contextConfig;
    }

    private void doLoadConfig(String contextConfigLocation) {
        //去除文件的classpath:开头字符串
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replaceAll("classpath:", ""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private void doScanner(String scanPackage) {
        //根据路径对"."进行转换成"/"
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        //扫描文件夹下的所有内容,可以理解成classPath文件夹
        for (File file : classPath.listFiles()) {
            //判断是否是文件夹，如果是文件夹则进行递归获取
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                //文件必须是.class格式，否则continue
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                //获取文件类名称,在初始化doInstance步骤时才根据文件名进行反射创建对象
                //包名+文件名；防止文件名称重复
                String className = (scanPackage + "." + file.getName().replace(".class", ""));
                //此步骤是获取类名称，并不在此处进行new对象，所以先进行存储
                regitryBeanClasses.add(className);
            }
        }

    }


    public List<ZbyBeanDefinition> loadBeanDefinitions() {
        List<ZbyBeanDefinition> result = new ArrayList<ZbyBeanDefinition>();
        for (String className : regitryBeanClasses) {
            try {
                Class<?> beanClass = Class.forName(className);
                //接口是不能实例化的，需要用它的实现类来实例化
                if(beanClass.isInterface()){
                    continue;
                }
                //保存对应的ClassName(全类名)，以及BeanName
                //region 规则
                //1、默认类名首字母小写，组装BeanDefinition对象存到list集合当中
                result.add(doCreateBeanDefinition(toLowerFistCase(beanClass.getSimpleName()), beanClass.getName()));
                //2、自定义
                //3、接口注入
                for(Class<?> i: beanClass.getInterfaces()){
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }
                //endregion
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private ZbyBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        ZbyBeanDefinition zbyBeanDefinition = new ZbyBeanDefinition();
        zbyBeanDefinition.setFactoryBeanName(beanName);
        zbyBeanDefinition.setBeanClassName(beanClassName);
        return zbyBeanDefinition;
    }

    private String toLowerFistCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
