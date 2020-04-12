package com.hh.spring.spring.framework.aop.aspect;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHAdvice.java
 * @Description 增强的method
 * @createTime 2020年04月12日
 */
@Data
public class HHAdvice {

    private Object aspect;
    private Method adviceMethod;
    private String throwName;

    public HHAdvice(Object aspect, Method adviceMethod) {
        this.aspect = aspect;
        this.adviceMethod = adviceMethod;
    }
}
