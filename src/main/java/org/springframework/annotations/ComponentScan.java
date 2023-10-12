package org.springframework.annotations;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ComponentScan {

     String[] packages();
}
