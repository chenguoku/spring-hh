package com.hh.spring.spring.framework.beans.support;

import com.hh.spring.spring.framework.beans.config.HHBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author chenguoku
 * @version 1.0.0
 * @ClassName HHBeanDefinitionReader.java
 * @Description 根据路径，加载bean，解析出来 BeanDefinition
 * @createTime 2020年04月07日 23:27:00
 */
public class HHBeanDefinitionReader {

    private Properties contextConfig = new Properties();
    //保存扫描的结果
    private List<String> registerBeanClasses = new ArrayList<String>();

    public HHBeanDefinitionReader(String... configLocations) {
        // 1.加载配置文件
        doLoadConfig(configLocations[0]);

        // 2.扫描配置文件中，需要加载到IOC中的Bean
        doScanner(contextConfig.getProperty("scanPackage"));
    }

    /**
     * 根据路径扫描 需要加载到 IOC中的Bean
     *
     * @author: chenguoku
     * @date: 2020/4/7
     */
    private void doScanner(String scanPackage) {
        // 获取到 配置的目录
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        //当成是一个classpath文件夹
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                //全类名=包名.类名
                String className = scanPackage + "." + file.getName().replace(".class", "");
                //Class.forName(className)
                registerBeanClasses.add(className);
            }
        }

    }

    /**
     * 加载配置文件
     *
     * @return:
     * @author: chenguoku
     * @date: 2020/4/7
     */
    private void doLoadConfig(String configLocation) {
        // 加载配置文件
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation.replaceAll("classpath:", ""));
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public List<HHBeanDefinition> loadBeanDefinitions() {
        List<HHBeanDefinition> result = new ArrayList<HHBeanDefinition>();
        try {
            for (String className : registerBeanClasses) {
                Class<?> beanClass = Class.forName(className);
                if (beanClass.isInterface()) {
                    continue;
                }
                //保存类对应的ClassName(全类名)
                //还有beanName
                //1.默认是 类名首字母小写
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
                //2.TODO：优化 自定义注入

                //3.接口注入
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Properties getConfig(){
        return this.contextConfig;
    }

    private HHBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        HHBeanDefinition beanDefinition = new HHBeanDefinition(beanName, beanClassName);
        return beanDefinition;
    }

    /**
     * 类名首字母小写： ClassName -> className
     *
     * @param simpleName 首字母大写的字符串
     * @return: 首字母小写的字符串
     * @author: chenguoku
     * @date: 2020/4/11
     */
    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }


}
