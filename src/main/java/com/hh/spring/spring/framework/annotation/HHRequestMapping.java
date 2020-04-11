package com.hh.spring.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHRequestMapping.java
 * @Description RequestMapping
 * @createTime 2020年04月11日
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HHRequestMapping {
    String value() default "";
}
