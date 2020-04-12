package com.hh.spring.spring.framework.webmvc.servlet;

import com.hh.spring.spring.framework.annotation.HHController;
import com.hh.spring.spring.framework.annotation.HHRequestMapping;
import com.hh.spring.spring.framework.annotation.HHResponseBody;
import com.hh.spring.spring.framework.context.HHApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHDispatcherServlet.java
 * @Description 负责任务调度，请求分发
 * @createTime 2020年04月07日 22:23:00
 */
public class HHDispatcherServlet extends HttpServlet {
    HHApplicationContext applicationContext;

    private List<HHHandlerMapping> handlerMappings = new ArrayList<HHHandlerMapping>();

    private Map<HHHandlerMapping, HHHandlerAdapter> handlerAdapters = new HashMap<HHHandlerMapping, HHHandlerAdapter>();

    private List<HHViewResolver> viewResolvers = new ArrayList<HHViewResolver>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception ,Detail:" + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //完成了对HandlerMapping的封装
        //完成了对方法返回值的封装ModelAndView

        //1. 通过URL获取一个HandlerMapping
        HHHandlerMapping handler = getHandler(req);
        if (handler == null) {
            processDispatchResult(req, resp, new HHModelAndView("404"), null);
            return;
        }

        //2.根据一个HandlerMapping获得一个HandlerAdapter
        HHHandlerAdapter ha = getHandlerAdapter(handler);

        //3.解析某一个方法的形参和返回值之后，统一封装为ModelAndView对象
        HHModelAndView mv = ha.handler(req, resp, handler);

        //就把ModelAndView变成一个ViewResolver
        processDispatchResult(req, resp, mv, handler.getMethod());

    }

    private HHHandlerAdapter getHandlerAdapter(HHHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }

        return this.handlerAdapters.get(handler);
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, HHModelAndView mv, Method method) throws Exception {
        if (mv == null || method == null || this.viewResolvers.isEmpty()) {
            return;
        }

        // 判断一个 是不是 @HHResponseBody修饰的方法
        if (method.isAnnotationPresent(HHResponseBody.class)) {
            // 使用@HHResponseBody修饰的 方法返回JSON字符串， 在 Adapter 执行得到的结果，存到了viewName属性中
            resp.getWriter().write(mv.getViewName());
            return;
        }

        // TODO ：优化 多个HHViewResolver
        for (HHViewResolver viewResolver : this.viewResolvers) {
            HHView hhView = viewResolver.resolveViewName(mv.getViewName());
            //直接往浏览器上输出
            hhView.render(mv.getModel(), req, resp);
            return;
        }
    }

    private HHHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (HHHandlerMapping hhHandlerMapping : handlerMappings) {
            Matcher matcher = hhHandlerMapping.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return hhHandlerMapping;
        }
        return null;

    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 初始化Spring核心IOC容器
        applicationContext = new HHApplicationContext(config.getInitParameter("contextConfigLocation"));

        // 初始化MVC 九大组件
        initStrategies(applicationContext);

        System.out.println("HH Spring framework is init");

    }

    private void initStrategies(HHApplicationContext context) {
//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    private void initViewResolvers(HHApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);

        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new HHViewResolver(templateRoot));
        }

    }

    private void initHandlerAdapters(HHApplicationContext context) {
        for (HHHandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new HHHandlerAdapter());
        }
    }

    private void initHandlerMappings(HHApplicationContext context) {
        if (context.getBeanDefinitionCount() == 0) {
            return;
        }

        for (String beanName : context.getBeanDefinitionNames()) {
            Object instance = applicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();
            if (!clazz.isAnnotationPresent(HHController.class)) {
                continue;
            }

            //相当于提取 class上配置的url
            String baseUrl = "";
            if (clazz.isAnnotationPresent(HHRequestMapping.class)) {
                HHRequestMapping annotation = clazz.getAnnotation(HHRequestMapping.class);
                baseUrl += annotation.value();
            }

            //只获取public方法上的 url
            for (Method method : clazz.getMethods()) {
                if (!method.isAnnotationPresent(HHRequestMapping.class)) {
                    continue;
                }

                //提取方法上的url
                HHRequestMapping requestMapping = method.getAnnotation(HHRequestMapping.class);

                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*", ".*")).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new HHHandlerMapping(pattern, method, instance));
                System.out.println("Mapped:" + regex + "," + method);
            }

        }

    }
}
