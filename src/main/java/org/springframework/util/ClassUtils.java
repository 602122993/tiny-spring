package org.springframework.util;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ClassUtils {
    public static <A extends Annotation> A findAnnotation(Class<?> target, Class<A> annoClass) {
        A a = target.getAnnotation(annoClass);
        for (Annotation anno : target.getAnnotations()) {
            Class<? extends Annotation> annoType = anno.annotationType();
            if (!annoType.getPackage().getName().equals("java.lang.annotation")) {
                A found = findAnnotation(annoType, annoClass);
                if (found != null) {
                    if (a != null) {
                        throw new RuntimeException("Duplicate @" + annoClass.getSimpleName() + " found on class " + target.getSimpleName());
                    }
                    a = found;
                }
            }
        }
        return a;
    }

    public static String getBeanName(Class<?> clazz) {
        // 使用正则表达式匹配最后一个.之后的部分，即类名
        String[] parts = clazz.getName().split("\\.");
        String className = parts[parts.length - 1];

        // 将类名的首字母小写
        return className.substring(0, 1).toLowerCase() + className.substring(1);
    }

    public static List<Method> findInitMethods(Class<?> clazz) {
        List<Method> methodList = new ArrayList<>();
        if(clazz.getSuperclass()!=null){
            methodList.addAll(findInitMethods(clazz.getSuperclass()));
        }
        for (Method method : clazz.getMethods()) {
            PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
            if(postConstruct!=null){
                methodList.add(method);
            }
        }
        return methodList;
    }

    public static List<Method> findDestroyMethods(Class<?> clazz) {
        List<Method> methodList = new ArrayList<>();
        if(clazz.getSuperclass()!=null){
            methodList.addAll(findInitMethods(clazz.getSuperclass()));
        }
        for (Method method : clazz.getMethods()) {
            PreDestroy postConstruct = method.getAnnotation(PreDestroy.class);
            if(postConstruct!=null){
                methodList.add(method);
            }
        }
        return methodList;
    }
}