package org.springframework.beans;


import java.util.HashMap;
import java.util.Map;

public class DefaultApplicationContext {

    private Map<String, Object> beanMap = new HashMap<>();

    public Object getBean(String name) {
        return beanMap.get(name);
    }

    public void registerBean(String name, Object bean) {
        beanMap.put(name, bean);
    }
}
