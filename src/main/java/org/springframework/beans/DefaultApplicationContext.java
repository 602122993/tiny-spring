package org.springframework.beans;


import org.springframework.annotations.Component;
import org.springframework.beans.config.BeanPostProcessor;
import org.springframework.resource.PropertyResolver;
import org.springframework.resource.ResourceResolver;
import org.springframework.util.BeanUtil;
import org.springframework.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    //全部的BeanPostProcessor
    private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();


    private PropertyResolver propertyResolver;


    public void init(Class<?> startClass) {
        //初始化容器

        //加载配置文件
        loadProperties();
        //扫描所有文件加载对应的bean
        loadBeanDefinition(startClass);
        //加载全部BeanPostProcessor
        loadBeanPostProcessor();
        //实例化所有bean
        createBean();
    }

    private void loadBeanPostProcessor() {
        //获取全部的BeanPostProcessor
        Map<String, BeanPostProcessor> beanPostProcessorMap = getBeansOfType(BeanPostProcessor.class);
        beanPostProcessors.addAll(beanPostProcessorMap.values());
    }


    public <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        Map<String, T> result = new HashMap<>();
        beanDefinitionMap.forEach((beanName, beanDefinition) -> {
            Class beanClass = beanDefinition.getClazz();
            if (clazz.isAssignableFrom(beanClass)) {
                T bean = (T) createBeanSingleton(beanDefinition);
                result.put(beanName, bean);
            }
        });
        return result;
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
        //在bean初始化之前执行的操作
        Object wrapperBean = applyBeanPostProcessorsBeforeInitialization(instance, beanDefinition.getName());
        //初始化bean
        initBean(beanDefinition, wrapperBean);
        //在bean初始化之前执行的操作
        wrapperBean = applyBeanPostProcessorsAfterInitialization(wrapperBean, beanDefinition.getName());
        beanMap.put(beanDefinition.getName(), wrapperBean);
        return instance;
    }

    private Object applyBeanPostProcessorsAfterInitialization(Object instance, String name) {
        Object wrapperBean = instance;
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            wrapperBean = beanPostProcessor.postProcessAfterInitialization(instance, name);
        }
        return wrapperBean;
    }

    private Object applyBeanPostProcessorsBeforeInitialization(Object instance, String beanName) {
        Object wrapperBean = instance;
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            wrapperBean = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
        }
        return wrapperBean;
    }

    private void initBean(BeanDefinition beanDefinition, Object instance) {
        beanDefinition.getInitMethodList().forEach(initMethod -> {
            try {
                initMethod.invoke(instance);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
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
        List<Method> initMethod = ClassUtils.findInitMethods(clazz);
        List<Method> destroyMethod = ClassUtils.findDestroyMethods(clazz);
        List<LifecycleMethod> initLifeCircleMethod = initMethod.stream()
                .map(LifecycleMethod::new)
                .collect(Collectors.toList());
        List<LifecycleMethod> destroyLifeCircleMethod = destroyMethod.stream()
                .map(LifecycleMethod::new)
                .collect(Collectors.toList());
        BeanDefinition beanDefinition = new BeanDefinition(name, clazz, initLifeCircleMethod, destroyLifeCircleMethod);
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    }

    public void close() {
        beanMap.forEach((beanName, bean) -> {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            beanDefinition.getDestroyMethodList().forEach(destroy -> {
                try {
                    destroy.invoke(bean);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

}
