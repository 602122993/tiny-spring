package org.springframework.util;

import org.springframework.annotations.Autowired;
import org.springframework.annotations.Value;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class BeanUtil {

    public static Map<String, Field> findNeedInjectPropertiesMap(Class<?> clazz) {
        Map<String, Field> needInjectMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Value value = field.getAnnotation(Value.class);
            if (value != null) {
                needInjectMap.put(value.value(), field);
            }
        }
        //注入父类字段
        if (clazz.getSuperclass() != null) {
            needInjectMap.putAll(findNeedInjectPropertiesMap(clazz.getSuperclass()));
        }
        return needInjectMap;
    }

    public static void injectFieldValue(Field field, Object instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Field> findNeedInjectBeanMap(Class<?> clazz) {
        Map<String, Field> needInjectMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            Autowired autowired = field.getAnnotation(Autowired.class);
            if (autowired != null) {
                needInjectMap.put(autowired.name() == null || autowired.name().isEmpty() ? field.getName() : autowired.name(), field);
            }
        }
        //注入父类字段
        if (clazz.getSuperclass() != null) {
            needInjectMap.putAll(findNeedInjectBeanMap(clazz.getSuperclass()));
        }
        return needInjectMap;
    }
}
