package com.spring.mvcframework.annotaion;

import java.lang.annotation.*;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/03/31 20:29
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZbyRequestMapping {
    String value() default "";
}
