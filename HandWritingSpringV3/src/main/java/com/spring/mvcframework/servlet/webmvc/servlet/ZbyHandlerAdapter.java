package com.spring.mvcframework.servlet.webmvc.servlet;

import com.spring.mvcframework.annotaion.ZbyRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/17 11:48
 */
public class ZbyHandlerAdapter {

    public ZbyModelAndView handler(HttpServletRequest req, HttpServletResponse resp, ZbyHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {

        /**
         * 行参列表
         * 将参数名称和参数的位置，这种关系保存起来
         */
        Map<String, Integer> paramIndexMapping = new HashMap<String, Integer>();

        //通过运行时状态去拿到注解的值
        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for (int j = 0; j < pa.length; j++) {
            for (Annotation a : pa[j]) {
                if (a instanceof ZbyRequestParam) {
                    String paramName = ((ZbyRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, j);
                    }
                }
            }
        }

        //初始化下
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class) {
                paramIndexMapping.put(parameterType.getName(), i);
            }
        }

        //去拼接实参列表
        Map<String, String[]> params = req.getParameterMap();

        Object[] paramvalues = new Object[parameterTypes.length];

        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(params.get(param.getKey()
                    .replaceAll("\\[|\\]]", "")
                    .replaceAll("s+", "")));
            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            //允许自定义的类型转换器
            Integer index = paramIndexMapping.get(param.getKey());
            //强转赋值
            paramvalues[index] = castStringValue(value, parameterTypes[index]);
        }

        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            Integer index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramvalues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            Integer index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramvalues[index] = req;
        }

        Object result = handler.getMethod().invoke(handler.getController(), paramvalues);
        if(result == null || result instanceof Void){
            return null;
        }

        //如果返回类型是ZbyModelAndView时则强转该对象
        boolean isModelAndView = handler.getMethod().getReturnType() == ZbyModelAndView.class;
        if(isModelAndView){
            return (ZbyModelAndView)result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> parameterType) {

        if (String.class == parameterType) {
            return value;
        } else if (Integer.class == parameterType) {
            return Integer.valueOf(value);
        } else if (Double.class == parameterType) {
            return Double.valueOf(value);
        } else {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
