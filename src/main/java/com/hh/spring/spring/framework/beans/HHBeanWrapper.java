package com.hh.spring.spring.framework.beans;

import lombok.Data;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHBeanWrapper.java
 * @Description BeanWrapper
 * @createTime 2020年04月11日
 */
@Data
public class HHBeanWrapper {

    private Object wrapperInstance;
    private Class<?> wrapperClass;

    public HHBeanWrapper(Object wrapperInstance) {
        this.wrapperInstance = wrapperInstance;
        this.wrapperClass = wrapperInstance.getClass();
    }
}
