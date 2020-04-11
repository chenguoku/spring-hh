package com.hh.spring.spring.framework.beans.config;

import lombok.Data;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHBeanDefinition.java
 * @Description Bean的定义类
 * @createTime 2020年04月07日 23:32:00
 */
@Data
public class HHBeanDefinition {

    private String factoryBeanName;
    private String beanClassName;

    public HHBeanDefinition() {
    }

    public HHBeanDefinition(String factoryBeanName, String beanClassName) {
        this.factoryBeanName = factoryBeanName;
        this.beanClassName = beanClassName;
    }
}
