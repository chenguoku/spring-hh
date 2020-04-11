package com.hh.spring.spring.framework.annotation;

import java.lang.annotation.*;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHController.java
 * @Description @Controller注解
 * @createTime 2020年04月11日
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HHController {
    String value() default "";
}
