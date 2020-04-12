package com.hh.spring.spring.framework.aop;

import com.hh.spring.spring.framework.aop.aspect.HHAdvice;
import com.hh.spring.spring.framework.aop.support.HHAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHJdkDynamicAopProxy.java
 * @Description 代理类
 * @createTime 2020年04月12日
 */
public class HHJdkDynamicAopProxy implements InvocationHandler {

    private HHAdvisedSupport config;

    public HHJdkDynamicAopProxy(HHAdvisedSupport config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Map<String, HHAdvice> advices = config.getAdvices(method);
        Object returnValue;
        try {
            invokeAdvice(advices.get("before"));

            returnValue = method.invoke(this.config.getTarget(), args);

            invokeAdvice(advices.get("after"));
        } catch (Exception e) {
            invokeAdvice(advices.get("afterThrow"));
            throw e;
        }

        return returnValue;
    }

    private void invokeAdvice(HHAdvice advice) {
        try {

            advice.getAdviceMethod().invoke(advice.getAspect());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), this.config.getTargetClass().getInterfaces(), this);
    }
}
