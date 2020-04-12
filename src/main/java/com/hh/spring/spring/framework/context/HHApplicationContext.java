package com.hh.spring.spring.framework.context;

import com.hh.spring.spring.framework.annotation.HHAutowired;
import com.hh.spring.spring.framework.annotation.HHController;
import com.hh.spring.spring.framework.annotation.HHService;
import com.hh.spring.spring.framework.aop.HHJdkDynamicAopProxy;
import com.hh.spring.spring.framework.aop.config.HHAopConfig;
import com.hh.spring.spring.framework.aop.support.HHAdvisedSupport;
import com.hh.spring.spring.framework.beans.HHBeanWrapper;
import com.hh.spring.spring.framework.beans.config.HHBeanDefinition;
import com.hh.spring.spring.framework.beans.support.HHBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHApplicationContext.java
 * @Description Spring的核心IOC容器
 * @createTime 2020年04月07日 22:27:00
 */
public class HHApplicationContext {

    private Map<String, HHBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String, HHBeanDefinition>();

    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();

    private Map<String, HHBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, HHBeanWrapper>();

    private HHBeanDefinitionReader reader;

    public HHApplicationContext(String... configLocations) {
        // 1. 加载配置文件
        reader = new HHBeanDefinitionReader(configLocations);

        // 2. 解析配置文件
        List<HHBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3. 把BeanDefinition缓存起来
        try {
            doRegisterBeanDefinition(beanDefinitions);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //4.依赖注入,如果要是 懒加载，初始化的时候，先不初始化 Bean，用到Bean的时候在初始化
        doAutowired();

    }

    private void doAutowired() {
        //调用getBean()
        //这一步，所有的bean并没有真正的实例化，还只是配置阶段
        for (Map.Entry<String, HHBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }

    }

    /**
     * Bean的实例化方法，DI是从这个方法开始的
     *
     * @return:
     * @author: chenguoku
     * @date: 2020/4/11
     */
    public Object getBean(String beanName) {
        // 1. 先拿到BeanDefinition配置信息
        HHBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        // 2. 反射实例化newInstance
        Object instance = instanceBean(beanName, beanDefinition);
        // 3. 封装成一个叫 BeanWrapper
        HHBeanWrapper hhBeanWrapper = new HHBeanWrapper(instance);
        // 4. 保存到IOC容器中
        this.factoryBeanInstanceCache.put(beanName, hhBeanWrapper);
        // 5. 执行依赖注入
        populateBean(beanName, beanDefinition, hhBeanWrapper);

        return hhBeanWrapper.getWrapperInstance();
    }

    /**
     * 依赖注入
     * 可能涉及到循环依赖
     * 用两次缓存，循环两次
     * 1.第一次读取结果为空的BeanDefinition存到第一个缓存
     * 2.等第一次循环之后，第二次循环在检查第一次的缓存，在进行赋值
     *
     * @return:
     * @author: chenguoku
     * @date: 2020/4/11
     */
    private void populateBean(String beanName, HHBeanDefinition beanDefinition, HHBeanWrapper beanWrapper) {

        Object instance = beanWrapper.getWrapperInstance();
        Class<?> clazz = beanWrapper.getWrapperClass();

        // TODO：优化 在Spring中 @Component
        if (!(clazz.isAnnotationPresent(HHController.class) || clazz.isAnnotationPresent(HHService.class))) {
            return;
        }

        // 把所有的包括 private/protected/default/public 修饰字段都取出来
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(HHAutowired.class)) {
                continue;
            }

            HHAutowired annotation = field.getAnnotation(HHAutowired.class);

            //如果用户没有自定义的beanName，就默认根据类型注入
            String autowiredBeanName = annotation.value().trim();
            if ("".equals(autowiredBeanName)) {
                // field.getType().getName(); 获取字段类型
                autowiredBeanName = field.getType().getName();
            }

            //暴力访问
            field.setAccessible(true);

            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }

                //ioc.getName() 相当于通过接口的全名拿到接口的实现的实例
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

        }

    }

    /**
     * 创建真正的对象
     *
     * @return: 实例化的对象
     * @author: chenguoku
     * @date: 2020/4/11
     */
    private Object instanceBean(String beanName, HHBeanDefinition beanDefinition) {

        String beanClassName = beanDefinition.getBeanClassName();

        Object instance = null;

        try {

            if (this.factoryBeanObjectCache.containsKey(beanName)) {
                //容器中存在bean
                instance = this.factoryBeanObjectCache.get(beanName);
            } else {
                //容器中没有bean，实例化bean
                Class<?> clazz = Class.forName(beanClassName);
                //默认类名首字母小写
                instance = clazz.newInstance();

                //==============AOP开始=====================
                //如果满足条件，就直接返回Proxy对象
                //1.加载AOP配置文件
                HHAdvisedSupport config = instantiationAopConfig();
                config.setTarget(instance);
                config.setTargetClass(clazz);
                config.init();

                //2.判断规则，要不要生成代理类，如果要就覆盖原生对象
                //如果不要就不做任何处理，返回原生对象
                if (config.pointCutMatch()) {
                    instance = new HHJdkDynamicAopProxy(config).getProxy();
                }
                //===============AOP结束====================

                //将生成的bean，存到容器中
                this.factoryBeanObjectCache.put(beanName, instance);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return instance;
    }

    /**
     * 加载AOP配置文件
     *
     * @return:
     * @author: chenguoku
     * @date: 2020/4/12
     */
    private HHAdvisedSupport instantiationAopConfig() {
        //TODO：优化 每个对象都初始化一遍，aop config，
        HHAopConfig config = new HHAopConfig();
        config.setPointCut(this.reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(this.reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(this.reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(this.reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(this.reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(this.reader.getConfig().getProperty("aspectAfterThrowingName"));

        return new HHAdvisedSupport(config);
    }

    private void doRegisterBeanDefinition(List<HHBeanDefinition> beanDefinitions) throws Exception {
        for (HHBeanDefinition beanDefinition : beanDefinitions) {
            if (this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The " + beanDefinition.getFactoryBeanName() + " is exist!");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }

    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
