package com.xhua.annotation;

import java.lang.annotation.*;

/**
 *   自定义Controller 注解，作用在类名上
 */

@Target(ElementType.TYPE)   //指定直接可作用在接口、类、枚举、注解上
@Retention(RetentionPolicy.RUNTIME)
@Documented

public @interface MyController {
    String value() default "";
}
