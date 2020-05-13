package com.spring.mvcframework.servlet.webmvc.servlet;

import com.spring.mvcframework.annotaion.ZbyController;
import com.spring.mvcframework.annotaion.ZbyRequestMapping;
import com.spring.mvcframework.servlet.context.ZbyApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用设计模式：
 * 1、委派模式
 * 职责：负责任务调度，任务分发
 *
 * @author zhaobinyang
 * @date 2020/04/11 16:42
 */
public class ZbyDisPathCherServlet extends HttpServlet {

    private ZbyApplicationContext zbyApplicationContext;

    private List<ZbyHandlerMapping> handlerMappings = new ArrayList<ZbyHandlerMapping>();

    private Map<ZbyHandlerMapping, ZbyHandlerAdapter> handlerAdapters = new HashMap<ZbyHandlerMapping, ZbyHandlerAdapter>();

    private List<ZbyViewResolver> viewResolvers = new ArrayList<ZbyViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6.委派，根据URL去找个一个对应的Mapping并返回
        try {
            //对HandlerMapping的封装
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                processDispatchResult(req, resp, new ZbyModelAndView("500"));
            } catch (Exception e2) {
                e2.printStackTrace();
                resp.getWriter().print("500 Exception,Detail:" + Arrays.toString(e.getStackTrace()));
            }
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1、初始化Spring核心IOC容器
        zbyApplicationContext = new ZbyApplicationContext(config.getInitParameter("contextConfigLocation"));

        //完成了IoC、DI和MVC部分对接

        //初始化策略
        initStrategies(zbyApplicationContext);
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、通过url获取一个HandlerMapping
        ZbyHandlerMapping handler = getHandler(req);

        if (handler == null) {
            processDispatchResult(req, resp, new ZbyModelAndView("404"));
            return;
        }

        //2、根据一个HandlerMapping获得一个HandlerAdapter(使用了适配器模式)
        ZbyHandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        //3、解析某一个方法的行参和返回值之后，统一封装为ModelAndView对象
        ZbyModelAndView mv = handlerAdapter.handler(req, resp, handler);

        //4、把ModelAndView变成一个ViewResolver
        processDispatchResult(req, resp, mv);

    }

    private ZbyHandlerAdapter getHandlerAdapter(ZbyHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        return this.handlerAdapters.get(handler);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ZbyModelAndView mv) throws Exception {
        if (mv == null) {
            return;
        }
        if (this.viewResolvers.isEmpty()) {
            return;
        }
        for (ZbyViewResolver viewResolver : this.viewResolvers) {
            ZbyView zbyView = viewResolver.resolveViewNames(mv.getViewName());
            //进行渲染
            zbyView.render(mv.getModel(), req, resp);
            return;
        }

    }

    private ZbyHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }

        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        for (ZbyHandlerMapping mapping : this.handlerMappings) {
            Matcher matcher = mapping.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return mapping;
        }
        return null;
    }

    /**
     * 初始化九大策略
     *
     * @param zbyApplicationContext
     */
    private void initStrategies(ZbyApplicationContext zbyApplicationContext) {
        //        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(zbyApplicationContext);
        //初始化参数适配器
        initHandlerAdapters(zbyApplicationContext);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(zbyApplicationContext);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initViewResolvers(ZbyApplicationContext context) {
        //获取文件路径
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new ZbyViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters(ZbyApplicationContext zbyApplicationContext) {
        for (ZbyHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new ZbyHandlerAdapter());
        }
    }

    private void initHandlerMappings(ZbyApplicationContext zbyApplicationContext) {
        if (this.zbyApplicationContext.getBeanDefinitionCount() == 0) {
            return;
        }

        for (String beanName : this.zbyApplicationContext.getBeanDefinitionNames()) {
            Object instance = zbyApplicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

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
                // //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new ZbyHandlerMapping(pattern, instance, method));
                System.out.println("Mapped:" + regex + "," + method);
            }
        }
    }
}
