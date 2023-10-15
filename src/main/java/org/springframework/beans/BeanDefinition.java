package org.springframework.beans;

import org.springframework.annotations.Component;
import org.springframework.annotations.ComponentScan;


public class BeanDefinition {

    //bean的类型
    Class<?> clazz;
    //bean 的名称
    String name;



    public BeanDefinition(String name, Class<?> clazz) {
        this.clazz = clazz;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
