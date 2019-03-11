package com.xhua.annotation;

import java.lang.annotation.*;

/**
 *  自定义 Resource 注解注入属性
 */

@Target({ ElementType.FIELD }) // 属性上的注解
@Retention(RetentionPolicy.RUNTIME)
@Documented

public @interface MyResource {
    String value() default "";
}
