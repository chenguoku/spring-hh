package com.hh.spring.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHRequestParam.java
 * @Description RequestParam
 * @createTime 2020年04月11日
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HHRequestParam {
    String value() default "";

    boolean required() default false;
}
