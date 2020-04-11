package com.hh.spring.spring.framework.webmvc.servlet;

import lombok.Data;

import java.util.Map;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHModelAndView.java
 * @Description ModelAndView
 * @createTime 2020年04月11日 15:35:00
 */
@Data
public class HHModelAndView {

    private String viewName;
    private Map<String, ?> model;

    public HHModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }

    public HHModelAndView(String viewName) {
        this.viewName = viewName;
    }
}
