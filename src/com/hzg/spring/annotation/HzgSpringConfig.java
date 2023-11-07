package com.hzg.spring.annotation;

/* 配置类，作用类似于原生spring的 bean.xml 容器配置文件
* 1.给注解传入的value值也同时作用于该配置文件，则可以通过反射获取到
*   配置类和注解，拿到注解中的 value 得到要扫描的路径.
*
* */

//扫描 com.hzg.spring.component 下的类
@ComponentScan(value = "com.hzg.spring.component")
public class HzgSpringConfig {
}
