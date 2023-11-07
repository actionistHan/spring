package com.hzg.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* 自定义的一个注解
* 1.@Target(ElementType.TYPE) 指定ComponentScan注解可以修饰 Type程序元素
* 2.@Retention(RetentionPolicy.RUNTIME) 指定ComponentScan注解 保留范围
* 3.String value() default ""; 表示ComponentScan 可以传入value属性
* */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentScan {
    String value() default "";
}
