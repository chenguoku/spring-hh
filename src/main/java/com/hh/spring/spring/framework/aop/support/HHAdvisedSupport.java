package com.hh.spring.spring.framework.aop.support;

import com.hh.spring.spring.framework.aop.aspect.HHAdvice;
import com.hh.spring.spring.framework.aop.config.HHAopConfig;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHAdvisedSupport.java
 * @Description AdvisedSupport 用于 判断目标对象，需要不需要 aop增强
 * @createTime 2020年04月12日
 */
@Data
public class HHAdvisedSupport {
    private HHAopConfig config;
    private Object target;
    private Class targetClass;
    private Pattern pointCutClassPattern;

    private Map<Method, Map<String, HHAdvice>> methodCache;

    public HHAdvisedSupport(HHAopConfig config) {
        this.config = config;
    }

    /**
     * 解析配置文件，获取Pattern，method对应的Before、After等等
     *
     * @return:
     * @author: chenguoku
     * @date: 2020/4/12
     */
    private void parse() {
        //把Spring的expression表达式 编程Java能识别的 Pattern
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");

        //保留专门匹配Class的正则
        String pointCutForClassRegex = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClassRegex.substring(pointCutForClassRegex.lastIndexOf(" ") + 1));

        //享元共享池，保存增强类和增强方法的
        methodCache = new HashMap<Method, Map<String, HHAdvice>>();

        //保存专门匹配方法的正则
        Pattern pointCutPattern = Pattern.compile(pointCut);
        try {

            Class<?> aspectClass = Class.forName(this.config.getAspectClass());
            HashMap<String, Method> aspectMethods = new HashMap<String, Method>();
            for (Method method : aspectClass.getMethods()) {
                aspectMethods.put(method.getName(), method);
            }

            for (Method method : this.targetClass.getMethods()) {
                String methodString = method.toString();

                //把方法后边 抛出的异常去掉
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }

                Matcher matcher = pointCutPattern.matcher(methodString);
                if (matcher.matches()) {
                    //匹配到 类的 具体方法了
                    HashMap<String, HHAdvice> advices = new HashMap<String, HHAdvice>();

                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore()))) {
                        advices.put("before", new HHAdvice(aspectClass.newInstance(), aspectMethods.get(config.getAspectBefore())));
                    }
                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter()))) {
                        advices.put("after", new HHAdvice(aspectClass.newInstance(), aspectMethods.get(config.getAspectAfter())));
                    }
                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow()))) {
                        HHAdvice advice = new HHAdvice(aspectClass.newInstance(), aspectMethods.get(config.getAspectAfterThrow()));
                        advice.setThrowName(config.getAspectAfterThrowingName());
                        advices.put("afterThrow", advice);
                    }

                    //跟目标代理类的业务方法和Advise建立一对多个关联关系，以便在Proxy类中获得
                    methodCache.put(method, advices);
                }


            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Map<String, HHAdvice> getAdvices(Method method) throws NoSuchMethodException {
        Map<String, HHAdvice> cache = methodCache.get(method);
        if (null == cache) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cache = methodCache.get(m);
            this.methodCache.put(m, cache);
        }
        return cache;
    }

    /**
     * 匹配bean，是否需要 增强
     *
     * @return: bean是否需要增强
     * @author: chenguoku
     * @date: 2020/4/12
     */
    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    public void init() {
        parse();
    }
}
