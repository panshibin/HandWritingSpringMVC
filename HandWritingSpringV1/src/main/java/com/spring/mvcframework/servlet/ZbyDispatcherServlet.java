package com.spring.mvcframework.servlet;

import com.spring.mvcframework.annotaion.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/03/31 20:22
 */
public class ZbyDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    //享元模式，缓存
    private List<String> classNames = new ArrayList<>();

    //IOC容器
    private Map<String, Object> ioc = new HashMap<String, Object>();

    //HandlerMapping容器
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    /**
     * 初始化阶段：
     * 1、调用init()方法           /加载配置文件
     * 2、IOC容器初始化            /Map<String,Object>
     * 3、扫描相关的类             /scan-package = "com.spring.mvcframework.*"
     * 4、创建实例并保存至容器      /通过反射机制将类实例化放入IOC容器中
     * 5、进行DI操作               /扫描IOC容器中的实例，给没有赋值的属性自动赋值
     * 6、初始化HandlerMapping     /将一个URL和一个Method进行一对一的关联映射Map<String,Method>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        //6.委派，根据URL去找个一个对应的Mapping并返回
        try {
            doDispatch(req, resp);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                resp.getWriter().print("500 Exception,Detail:" + Arrays.toString(e.getStackTrace()));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String url = req.getRequestURI();
        //获取请求路径
        String contextPath = req.getContextPath();
        //获取绝对路径转换成相对路径
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        // 判断url是否在handlerMapping中
        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().print("404 Not Found!");
            return;
        }

        //获取请求所有参数
        Map<String, String[]> parameters = req.getParameterMap();

        Method method = this.handlerMapping.get(url);

        //获取行参列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] paramValues = new Object[parameterTypes.length];

        //遍历获取行参类型并赋值
        for (int i = 0; i < parameterTypes.length; i++) {
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
            } else if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
            } else if (parameterType == String.class) {
                //通过运行时状态去拿到注解的值
                Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                for (int j = 0; j < parameterAnnotations.length; j++) {
                    for (Annotation a : parameterAnnotations[j]) {
                        if (a instanceof ZbyRequestParam) {
                            String paramName = ((ZbyRequestParam) a).value();
                            if (!"".equals(paramName.trim())) {
                                //在所有请求参数中获取当前的paramName值
                                String value = Arrays.toString(parameters.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s+", ",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }

            }
        }

        try {
            String beanName = toLowerFistCase(method.getDeclaringClass().getSimpleName());
            //利用反射赋值
            method.invoke(ioc.get(beanName), paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //=============IOC部分=============//

        //3.初始化IOC容器，将扫描的相关类，保存到IOC容器中
        doInstance();

        //=============AOP部分=============//
        //=============DI部分=============//

        //4.完成依赖注入
        doAutowired();

        //5.初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("Zby spring framework");
     }

    private void doInitHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }


        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();

            //如果这个类没有加ZbyController注解则跳过
            if (!clazz.isAnnotationPresent(ZbyController.class)) {
                continue;
            }

            //获取ZbyRequestMapping的Url获取后，对publice的url进行拼接，形成一个完整的请求url
            String baseUrl = "";
            if (clazz.isAnnotationPresent(ZbyRequestMapping.class)) {
                ZbyRequestMapping requestMapping = clazz.getAnnotation(ZbyRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取publice的方法
            for (Method method : clazz.getMethods()) {

                //如果这个方法没有加类ZbyRequestMapping注解则跳过
                if (!method.isAnnotationPresent(ZbyRequestMapping.class)) {
                    continue;
                }
                //提取每个方法上配置的url
                ZbyRequestMapping requestMapping = method.getAnnotation(ZbyRequestMapping.class);

                //通过正则表达式，对多个"/"进行一个转换
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapped:" + url + "," + method);

            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            //获取所有字段,包含publice、private、protected、default修饰的字段
            for (Field field : entry.getValue().getClass().getDeclaredFields()) {
                if (!field.isAnnotationPresent(ZbyAutowired.class)) {
                    continue;
                }

                ZbyAutowired autowired = field.getAnnotation(ZbyAutowired.class);
                //如果用户没有自定义的beanName，就默认根据类型注入
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    //ield.getType().getName() 获取字段类型
                    beanName = field.getType().getName();
                }

                //如果存在private类型，则需要暴力访问
                field.setAccessible(true);
                try {
                    //接口的实现类（相当于通过接口的全名拿到接口的实现的实例），拿到后最后通过set赋值
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        //利用反射创建对象
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);

                //只有类使用指定的注解才运行进行交由IOC管理
                if (clazz.isAnnotationPresent(ZbyController.class)) {

                    //首字母小写,用做key
                    String beanName = toLowerFistCase(clazz.getSimpleName());
                    //反射获取对象,用作value
                    Object instance = clazz.newInstance();

                    //存入ioc容器
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(ZbyService.class)) {
                    //1、在多个包下出现相同的类名，只能自己起一个全局唯一的自定义名字
                    String beanName = clazz.getAnnotation(ZbyService.class).value();
                    if ("".equals(beanName.trim())) {
                        //2、默认的类名首字母小写
                        beanName = toLowerFistCase(clazz.getSimpleName());
                    }

                    //反射获取对象,用作value
                    Object instance = clazz.newInstance();
                    //存入ioc容器
                    ioc.put(beanName, instance);

                    //3、如果是接口
                    //判断有多少个实现类，如果只有一个，默认就选中这个实现类
                    //如果有多个，只能抛异常

                    //获取所有接口
                    for (Class<?> i : clazz.getInterfaces()) {
                        //如果接口名称存在则抛出异常
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The" + i.getName() + "is exists!");
                        }
                        ioc.put(i.getName(), instance);

                    }
                } else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String toLowerFistCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
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
                classNames.add(className);
            }
        }

    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
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
}
