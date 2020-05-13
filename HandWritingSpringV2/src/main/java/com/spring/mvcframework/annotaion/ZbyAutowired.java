package com.spring.mvcframework.annotaion;

import java.lang.annotation.*;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/03/31 20:26
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZbyAutowired {
    String value() default "";
}
