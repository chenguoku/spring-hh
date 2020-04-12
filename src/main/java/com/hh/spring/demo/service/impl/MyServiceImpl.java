package com.hh.spring.demo.service.impl;

import com.hh.spring.demo.service.MyService;
import com.hh.spring.spring.framework.annotation.HHService;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName MyServiceImpl.java
 * @Description service
 * @createTime 2020年04月11日
 */
@HHService
public class MyServiceImpl implements MyService {
    @Override
    public String test() {
        System.out.println("test");
        return "Hello World!";
    }
}
