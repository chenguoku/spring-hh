package com.hh.spring.spring.framework.webmvc.servlet;

import lombok.Data;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHHandlerMapping.java
 * @Description HandlerMapping
 * @createTime 2020年04月11日 14:45:00
 */
@Data
public class HHHandlerMapping {
    private Pattern pattern;    //URL
    private Method method;      //对应的method方法
    private Object controller;  //Method对应的实例对象

    public HHHandlerMapping(Pattern pattern, Method method, Object controller) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }

}
