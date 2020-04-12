package com.hh.spring.demo.aspect;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName LogAspect.java
 * @Description 切面类
 * @createTime 2020年04月12日
 */
public class LogAspect {

    //方法之前执行
    public void before() {
        System.out.println("方法之前执行");
    }

    //方法之后执行
    public void after() {
        System.out.println("方法之后执行");
    }

    //方法抛出异常执行
    public void afterThrowing() {
        System.out.println("出现异常");
    }

}
