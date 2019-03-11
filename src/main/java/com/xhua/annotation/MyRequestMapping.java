package com.xhua.annotation;

import java.lang.annotation.*;

/**
 *  自定义 RequestMapping 注解，作用在类名和方法上，一级二级URL
 */

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented

public @interface MyRequestMapping {
    String value() default "";
}
