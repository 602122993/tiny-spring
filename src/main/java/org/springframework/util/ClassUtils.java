package org.springframework.util;

import java.lang.annotation.Annotation;

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
}