package com.hzg.spring.annotation;

import com.hzg.spring.component.MyComponent;
import com.hzg.spring.component.UserAction;
import com.hzg.spring.component.UserDao;
import com.hzg.spring.component.UserService;

public class HzgSpringApplicationContextTest {
    public static void main(String[] args) {
        //创建ioc容器，传入配置类(通过配置文件则传入的是.xml文件)
        HzgSpringApplicationContext ioc = new HzgSpringApplicationContext(HzgSpringConfig.class);

        //容器类中106行 转化了将类名首字母小写作为id
        MyComponent myComponent = (MyComponent)ioc.getBean("aA");
        System.out.println("myComponent = " + myComponent);

        UserAction userAction = (UserAction)ioc.getBean("userAction");
        System.out.println("userAction = "+userAction);

        UserDao userDao = (UserDao) ioc.getBean("userDao");
        System.out.println("userDao = "+userDao);

        UserService userService = (UserService)ioc.getBean("userService");
        System.out.println("userService = "+userService);

        System.out.println("OK");
    }
}
