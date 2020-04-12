package com.hh.spring.spring.framework.aop.config;

import lombok.Data;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHAopConfig.java
 * @Description AopConfig
 * @createTime 2020年04月12日
 */
@Data
public class HHAopConfig {
    private String pointCut;
    private String aspectClass;
    private String aspectBefore;
    private String aspectAfter;
    private String aspectAfterThrow;
    private String aspectAfterThrowingName;
}
