package org.springframework.beans;

import org.springframework.annotations.Component;
import org.springframework.annotations.ComponentScan;

import java.lang.reflect.Method;
import java.util.List;


public class BeanDefinition {

    //bean的类型
    Class<?> clazz;
    //bean 的名称
    String name;
    //初始化方法
    List<LifecycleMethod> initMethodList;
    //销毁方法
    List<LifecycleMethod> destroyMethodList;

    public BeanDefinition(String name, Class<?> clazz, List<LifecycleMethod> initMethodList, List<LifecycleMethod> destroyMethodList) {
        this.clazz = clazz;
        this.name = name;
        this.initMethodList = initMethodList;
        this.destroyMethodList = destroyMethodList;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<LifecycleMethod> getInitMethodList() {
        return initMethodList;
    }

    public List<LifecycleMethod> getDestroyMethodList() {
        return destroyMethodList;
    }
}
