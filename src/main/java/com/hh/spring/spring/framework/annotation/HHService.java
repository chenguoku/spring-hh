package com.hh.spring.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHService.java
 * @Description Service注解
 * @createTime 2020年04月11日
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HHService {
    String value() default "";
}
