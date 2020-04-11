package com.hh.spring.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHAutowired.java
 * @Description Autowired注解
 * @createTime 2020年04月11日
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HHAutowired {
    String value() default "";
}
