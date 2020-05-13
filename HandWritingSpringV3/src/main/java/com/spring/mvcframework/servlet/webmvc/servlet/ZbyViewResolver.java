package com.spring.mvcframework.servlet.webmvc.servlet;

import java.io.File;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/04/23 15:15
 */
public class ZbyViewResolver {
    //模版后缀名
    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";
    private File templateRootDir;

    public ZbyViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(templateRootPath);
    }

    public ZbyView resolveViewNames(String viewName) {
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new ZbyView(templateFile);
    }
}