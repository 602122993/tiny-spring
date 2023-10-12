package org.springframework.beans;


import org.springframework.annotations.Component;
import org.springframework.resource.ResourceResolver;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultApplicationContext {


    //保存beanDefinition
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    //保存实例化后的bean
    private final Map<String, Object> beanMap = new HashMap<>();


    public void init(Class<?> startClass) {
        //初始化容器
        //扫描所有文件加载对应的bean
        loadBeanDefinition(startClass);
    }

    private void loadBeanDefinition(Class<?> startClass) {
        //获取需要扫描的所有包名
        List<String> packageNames = ResourceResolver.findScanPackageName(startClass);
        packageNames.forEach(packageName -> {
            List<Class<?>> beanClassList;
            try {
                beanClassList = ResourceResolver.scanPackage(packageName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            beanClassList.forEach(clazz -> {
                Component component = ClassUtils.findAnnotation(clazz, Component.class);
                if (component != null) {
                    //如果被注解标记则注册到beanDefinitionMap中
                    registerBeanDefinition(ClassUtils.getBeanName(clazz), clazz);
                }
            });
        });

    }

    public Object getBean(String name) {
        //优先从缓存中获取
        Object bean = beanMap.get(name);
        if (bean != null) {
            return bean;
        }
        //缓存中没有获取beanDefinition
        BeanDefinition beanDefinition = beanDefinitionMap.get(name);
        //实例化对象放入缓存
        Object instance = newInstance(beanDefinition);
        beanMap.put(name, instance);
        return instance;
    }

    private Object newInstance(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getClazz();
        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return instance;
    }

    public void registerBeanDefinition(String name, Class<?> clazz) {
        BeanDefinition beanDefinition = new BeanDefinition(name, clazz);
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    }

    public void registerBean(String name, Object bean) {
        beanMap.put(name, bean);
    }


}
