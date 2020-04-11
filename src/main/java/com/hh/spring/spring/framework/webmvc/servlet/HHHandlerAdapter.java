package com.hh.spring.spring.framework.webmvc.servlet;

import com.alibaba.fastjson.JSON;
import com.hh.spring.spring.framework.annotation.HHRequestParam;
import com.hh.spring.spring.framework.annotation.HHResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHHandlerAdapter.java
 * @Description handlerAdapter
 * @createTime 2020年04月11日
 */
public class HHHandlerAdapter {

    public HHModelAndView handler(HttpServletRequest req, HttpServletResponse resp, HHHandlerMapping handler) throws InvocationTargetException, IllegalAccessException {

        //保存形参列表
        //将参数名称和参数的位置，这种关系保存起来
        // paramIndexMapping 参数和参数的位置
        HashMap<String, Integer> paramIndexMapping = new HashMap<String, Integer>();

        //拿到运行时的，参数 及 位置  pa
        Annotation[][] pa = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof HHRequestParam) {
                    String paramName = ((HHRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }
        }

        //判断方法参数中 是否有 request和response，如果有  记录其位置
        // paramTypes 方法的参数数组
        Class<?>[] paramTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            if (paramType == HttpServletRequest.class || paramType == HttpServletResponse.class) {
                paramIndexMapping.put(paramType.getName(), i);
            }
        }

        // 去拼接实参列表,有的参数 可能是 数组
        // params 参数名和参数值
        Map<String, String[]> params = req.getParameterMap();

        // 新建的  paramValues，存储转换后 参数列表的值
        Object[] paramValues = new Object[paramTypes.length];

        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(params.get(param.getKey()))
                    .replaceAll("\\[|\\]", "")
                    .replaceAll("\\s+", ",");
            if (!paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }

            Integer index = paramIndexMapping.get(param.getKey());

            //允许自定义的类型转换器Converter
            paramValues[index] = castStringValue(value, paramTypes[index]);
        }

        // 给方法 参数的 request 和 response 赋值
        if (paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            Integer index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if (paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        //执行方法
        Object result = handler.getMethod().invoke(handler.getController(), paramValues);

        if (result == null || result instanceof Void) {
            return null;
        }

        boolean isResponseBody = handler.getMethod().isAnnotationPresent(HHResponseBody.class);
        if (isResponseBody) {
            String string = JSON.toJSONString(result);
            return new HHModelAndView(string);
        }

        //将结果封装成 ModelAndView
        boolean isModelAndView = handler.getMethod().getReturnType() == HHModelAndView.class;
        if (isModelAndView) {
            return (HHModelAndView) result;
        }

        return null;
    }

    private Object castStringValue(String value, Class<?> paramType) {
        if (String.class == paramType) {
            return value;
        } else if (Integer.class == paramType) {
            return Integer.valueOf(value);
        } else if (Double.class == paramType) {
            return Double.valueOf(value);
        } else {
            if (value != null) {
                return value;
            }
            return null;
        }

    }
}
