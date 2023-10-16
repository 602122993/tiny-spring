package org.springframework.beans;


import org.springframework.annotations.Component;
import org.springframework.resource.PropertyResolver;
import org.springframework.resource.ResourceResolver;
import org.springframework.util.BeanUtil;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultApplicationContext {


    //保存beanDefinition
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    //保存实例化后的bean
    private final Map<String, Object> beanMap = new HashMap<>();
    //保存正在实例化中的bean
    private final Map<String, Object> earlyBeanMap = new HashMap<>();


    private PropertyResolver propertyResolver;


    public void init(Class<?> startClass) {
        //初始化容器
        //加载配置文件
        loadProperties();
        //扫描所有文件加载对应的bean
        loadBeanDefinition(startClass);
        //实例化所有bean
        createBean();
    }

    private void createBean() {
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            createBeanSingleton(beanDefinition);
        });
    }

    private Object createBeanSingleton(BeanDefinition beanDefinition) {
        if (beanMap.get(beanDefinition.getName()) != null) {
            return beanMap.get(beanDefinition.getName());
        }
        if (earlyBeanMap.get(beanDefinition.getName()) != null) {
            return earlyBeanMap.get(beanDefinition.getName());
        }
        //bean的实例。
        Object instance = newInstance(beanDefinition);
        //将bean放入缓存中
        earlyBeanMap.put(beanDefinition.getName(), instance);
        //注入配置
        injectProperties(beanDefinition, instance);
        //注入其他bean
        injectBean(beanDefinition, instance);
        beanMap.put(beanDefinition.getName(), instance);
        return instance;
    }

    private void injectBean(BeanDefinition beanDefinition, Object instance) {
        Map<String, Field> needValueInjectMap = BeanUtil.findNeedInjectBeanMap(beanDefinition.getClazz());
        needValueInjectMap.forEach((name, field) -> {
            //需要注入，根据类型查询BeanDefinition
            List<BeanDefinition> matchBeanDefinitionList = beanDefinitionMap.values().stream()
                    .filter(obj -> obj.getClazz().equals(field.getType()))
                    .collect(Collectors.toList());
            if (matchBeanDefinitionList.isEmpty()) {
                throw new RuntimeException("No Bean Type +" + field.getType());
            }
            if (matchBeanDefinitionList.size() == 1) {
                //只有一个类型匹配
                BeanUtil.injectFieldValue(field, instance, createBeanSingleton(matchBeanDefinitionList.get(0)));
            } else {
                //多个类型匹配 按照名称进行匹配
                List<BeanDefinition> nameMatchList = matchBeanDefinitionList.stream()
                        .filter(matchBeanDefinition -> matchBeanDefinition.getName().equals(field.getName()))
                        .collect(Collectors.toList());
                if (nameMatchList.isEmpty()) {
                    throw new RuntimeException("Duplication Bean Type +" + field.getType());
                }
                //由于beanDefinition不会有重复的，所以不存在多个beanName匹配，直接注入
                BeanUtil.injectFieldValue(field, instance, createBeanSingleton(matchBeanDefinitionList.get(0)));
            }

        });
    }

    private void injectProperties(BeanDefinition beanDefinition, Object instance) {
        //遍历该bean的属性，看看是否有被@Value注解的属性
        Map<String, Field> needValueInjectMap = BeanUtil.findNeedInjectPropertiesMap(beanDefinition.getClazz());
        //进行配置注入
        needValueInjectMap.forEach((key, field) -> BeanUtil.injectFieldValue(field, instance, getPropertiesValue(key, field.getType())));
    }

    private void loadProperties() {
        try {
            //默认读取application.properties的内容
            String filePath = Objects.requireNonNull(PropertyResolver.class.getClassLoader().getResource("application.properties")).getFile();
            Properties properties = new Properties();
            //加载内容
            properties.load(Files.newInputStream(new File(filePath).toPath()));
            propertyResolver = new PropertyResolver(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getPropertiesValue(String key, Class<?> clazz) {
        return propertyResolver.getProperty(key, clazz);
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
        return beanMap.get(name);
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
