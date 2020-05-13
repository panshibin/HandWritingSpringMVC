package com.spring.mvcframework.annotaion;

import java.lang.annotation.*;

/**
 * 类描述:
 *
 * @author zhaobinyang
 * @date 2020/03/31 20:27
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZbyController {
    String value() default "";
}
