package org.springframework.beans;

import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

public class LifecycleMethod {
    private final Method method;


    public LifecycleMethod(Method method) {
        if (method.getParameterCount() != 0) {
            throw new IllegalStateException("Lifecycle annotation requires a no-arg method: " + method);
        }
        this.method = method;
    }

    public Method getMethod() {
        return this.method;
    }

    public void invoke(Object target) throws Throwable {
        this.method.invoke(target);
    }

}
