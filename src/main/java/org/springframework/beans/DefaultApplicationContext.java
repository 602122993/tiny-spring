package org.springframework.beans;


import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class DefaultApplicationContext {
    //保存beanDefinition
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();
    //保存实例化后的bean
    private final Map<String, Object> beanMap = new HashMap<>();

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
        beanMap.put(name,instance);
        return instance;
    }

    private Object newInstance(BeanDefinition beanDefinition) {
        Class<?> clazz = beanDefinition.getClazz();
        Object instance = null;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException   | IllegalAccessException  e) {
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
