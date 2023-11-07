package com.hzg.spring.annotation;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;


/* 容器类，作用类似于Spring原生ioc容器
*
* */
public class HzgSpringApplicationContext {
    private Class configClass; //存放传进构造器的配置类，供将来其他地方跨方法使用

    //ioc存放通过反射创建的对象(基于注解方式)
    private final ConcurrentHashMap<String, Object> ioc = new ConcurrentHashMap<>();

    //构造器
    //参数：传入一个Class反射类型的类
    public HzgSpringApplicationContext(Class configClass) {
        this.configClass = configClass; //用this接收的目的是：
                     // 将来configClass这个配置类可能不止在构造器内使用，所以创建了一个configClass属性来存放.
                    // 拿到配置类的class类型就拿到了全部信息包括注解的信息，也就拿到了注解的value值(地址)从而得到要扫描的包.

        //一、获取扫描包
        //1.先得到HspSpringConfig配置类的@ComponentScan(value = "com.hzg.spring.component")
        //.getDeclaredAnnotation 通过反射类调用方法可以拿到反射类上面的注解；.getDeclaredAnnotation可以在运行时获取类、方法、字段等元素上的注解信息，并返回一个注解对象
        ComponentScan componentScan = (ComponentScan)this.configClass.getDeclaredAnnotation(ComponentScan.class);
        //2.通过componentScan的value获得要扫描的包 path接收
        //在注解中配置了value 当作一个属性，对应一个方法可直接返回你配置的value内容.
        String path = componentScan.value();
        //System.out.println("path = " + path);


        //二、得到要扫描包下的所有资源(类.class)=>是从out项目目录中拿，out->production->spring是项目目录，com下是自己的包
        //1.得到类的加载器 .getClassLoader
        /*-类加载器作用：classLoader,就是负责把磁盘上的.class文件 ，加载到JVM内存中，并生成 java.lang.Class类的一个实例
          -每一个class对象都有一个getClassLoader()方法，得到是谁把我从.class文件加载到内存中变成Class对象的*/
        ClassLoader classLoader = HzgSpringApplicationContext.class.getClassLoader();
        //2.通过类的加载器获取到要扫描包的资源 url ->类似一个路径
        //URL resource = classLoader.getResource("com/hzg/spring/component");
        path = path.replace(".","/"); //后面字符串的替换前面的,把.替换成路径间隔符/ 。
        //.getResource 得到文件路径
        URL resource = classLoader.getResource(path); //.getResource()得到文件磁盘下全路径
        //System.out.println("resource = "+resource);  //结果：resource = file:/D:/javaData/hsp/daima/hsp_spring/spring/out/production/spring/com/hzg/spring/component
        //3.将要加载的资源(.class)路径下的文件进行遍历=>io
        File file = new File(resource.getFile()); //resource.getFile()返回一个文件，也可能是目录=>目录或者文件夹也是一种特殊的文件
        //System.out.println("file = "+file);  //结果：file = D:\javaData\hsp\daima\hsp_spring\spring\out\production\spring\com\hzg\spring\component
        if(file.isDirectory()){ //.isDirectory() 判断是否是个目录
            File[] files = file.listFiles(); //得到一个 File 类型的数组，返回的是该目录中的文件和目录.
            for (File f : files) {
                System.out.println("=================");
                System.out.println(f.getAbsolutePath()); //.getAbsolutePath()获取在磁盘上的全路径out目录下的


                //三、获取全类名，反射对象，放入容器
                // 结果：D:\javaData\hsp\daima\hsp_spring\spring\out\production\spring\com\hzg\spring\component\UserService.class
                //获取到com.hzg.spring.component.UserService
                String fileAbsolutePath = f.getAbsolutePath(); //拿到文件全路径赋值给fileAbsolutePath
                //System.out.println(fileAbsolutePath+"= fileAbsolutePath");

                //这里只处理.class文件
                if(fileAbsolutePath.endsWith(".class")) { //只有以.class结尾的数据才做处理; "字符串A".endsWith("B")用于测试字符串A是否以指定的B后缀结束。

                    //1.获取到类名 放入className
                    //.substring 字符串切割、截取字符串 传入beanIndexOf和endIndexOf，beanIndexOf包括当前起始位置,endIndexOf不包括 [)
                    //.lastIndexOf 返回指定子字符串在此字符串中最右边出现处的索引，如果此字符串中没有这样的字符，则返回 -1
                    //.indexOf 返回指定字符在字符串中第一次出现处的索引
                    String className =
                            fileAbsolutePath.substring(fileAbsolutePath.lastIndexOf("\\") + 1, fileAbsolutePath.indexOf(".class"));
                    //System.out.println("className = "+className); //运行结果：className = MyComponent
                    //2.获取类的完整路径(全类名)
                    //解读 path.replace("\\",".") => com.hzg.spring.component
                    String classFullName = path.replace("/", ".") + "." + className;
                    //System.out.println("classFullName = "+classFullName);

                    //3.判断该类是不是需要注入容器，就看该类是不是有@Component @Service..
                    try {
                        //这时得到该类的Class对象,下面两种方法都可以通过反射加载类
                        //Class.forName(classFullName) 会调用反射得到的类的静态方法
                        //Class.loadClass(classFullName) 不会调用该类的静态方法,只是返回类的信息  两种方法使用的类加载器也不同
                        Class<?> aClass = classLoader.loadClass(classFullName);
                        if(aClass.isAnnotationPresent(Component.class) || //.isAnnotationPresent 判断该类上有没有这个注解
                             aClass.isAnnotationPresent(Controller.class) ||
                             aClass.isAnnotationPresent(Service.class) ||
                             aClass.isAnnotationPresent(Repository.class)){


                            //Component注解指定了value，分配制定了id
                            if(aClass.isAnnotationPresent(Component.class)){ //因为是通过扫描注解(4个注解中有其中1个就会进入ioc)进来的类具体的注解 判断是否是Component注解
                                //获取到注解
                                Component component = aClass.getDeclaredAnnotation(Component.class); //因为类上不止一个注解，所以先获取Component注解
                                String id = component.value();
                                if(!"".endsWith(id)){ //效果：判断value不为空则将value值当作className；
                                                        /* 解读：空字符串以id为后缀结束(id为空)时,使用122行首字母小写作为默认id,所以要使表达式取反,也就是不为空时进行对id赋值,
                                                            因为不知道id具体是什么，所以不能从正面去判断字符串以具体id结尾 T则进入赋值,而是反向判断id不为空则进入赋值，逻辑*/
                                    className = id;
                                }
                            }
                            if(aClass.isAnnotationPresent(Controller.class)){
                                //获取到注解
                                Controller controller = aClass.getDeclaredAnnotation(Controller.class);
                                String id = controller.value();
                                if(!"".endsWith(id)){
                                    className = id;
                                }
                            }
                            if(aClass.isAnnotationPresent(Service.class)){
                                //获取到注解
                                Service controller = aClass.getDeclaredAnnotation(Service.class);
                                String id = controller.value();
                                if(!"".endsWith(id)){
                                    className = id;
                                }
                            }
                            if(aClass.isAnnotationPresent(Repository.class)){
                                //获取到注解
                                Repository controller = aClass.getDeclaredAnnotation(Repository.class);
                                String id = controller.value();
                                if(!"".endsWith(id)){
                                    className = id;
                                }
                            }


                            //这时就可以反射对象，并放入容器中
                                /*使用.newInstance()方法的时候，就必须保证：
                                    1、这个类已经加载,
                                    2、这个类已经连接了
                                  上面两个步骤是Class的静态方法forName()所完成的，这个静态方法调用了启动类加载器
                                 */
                            Class<?> clazz = Class.forName(classFullName); //返回一个类
                            Object instance = clazz.newInstance(); //使用.newInstance()生成一个实例化对象
                            //放入容器
                            //ioc.put(className,instance);
                            //放容器，将类名的首字母小写作为id
                                /* StringUtils 提供了一个字符串首字母小写的方法.uncapitalize */
                            ioc.put(StringUtils.uncapitalize(className),instance); //疑问：在 className = id => AA 后，这一步又给重新赋为小写开头了，而且是最终通过aA这个id使用bean
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
    //编写方法返回容器中的对象
    public Object getBean(String name){
        return ioc.get(name);
    }
}
      /* bean的加载过程：
      * 1.在测试类中创建ioc容器，加载传入的配置类，通过反射拿到配置类的全部信息，从而得到配置类上的注解，进而拿到value知道要扫描的包的路径；
      * 2.开始获取扫描包下的所有资源(第二步)，判断哪个类有注解(第三步)，创建对象并放入ioc容器并且用类首字母小写作为key放入集合 也就是id；
      * 3.编写一个getBean方法，可以让测试类通过传入id在ioc容器找到相应的对象来返回。
      * 4.无，凑数。单纯不想3结尾。
      *
      * */
