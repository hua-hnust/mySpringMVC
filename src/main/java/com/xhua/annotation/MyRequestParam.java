package com.xhua.annotation;

import java.lang.annotation.*;

/**
 *  自定义 RequestParam 注解，作用在参数上
 */

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented

public @interface MyRequestParam {
    String value() default "";
}
