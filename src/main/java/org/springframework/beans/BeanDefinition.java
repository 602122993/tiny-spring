package org.springframework.beans;

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
