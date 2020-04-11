package com.hh.spring.spring.framework.webmvc.servlet;

import java.io.File;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHViewResolver.java
 * @Description ViewResolver
 * @createTime 2020年04月11日 15:07:00
 */
public class HHViewResolver {

    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File templateRootDir;

    public HHViewResolver(String templateRoot) {
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        templateRootDir = new File(templateRootPath);
    }

    public HHView resolveViewName(String viewName) {
        if (null == viewName || "".equals(viewName.trim())) {
            return null;
        }
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX) ? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);

        File templateFile = new File((templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new HHView(templateFile);

    }
}
