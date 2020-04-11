package com.hh.spring.demo.controller;

import com.hh.spring.demo.service.MyService;
import com.hh.spring.spring.framework.annotation.*;
import com.hh.spring.spring.framework.webmvc.servlet.HHModelAndView;

import java.util.HashMap;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName MyController.java
 * @Description TODO
 * @createTime 2020年04月11日 14:05:00
 */
@HHController
@HHRequestMapping("/web")
public class MyController {

    @HHAutowired
    private MyService myService;

    @HHRequestMapping("/test")
    @HHResponseBody
    public String test() {
        return myService.test();
    }

    @HHRequestMapping("/first.html")
    public HHModelAndView query(@HHRequestParam("name") String name) {
        HashMap<String, Object> model = new HashMap<String, Object>();
        model.put("name", name);
        model.put("data", "Hello World");
        model.put("token", "123456");

        return new HHModelAndView("first.html", model);
    }


}
