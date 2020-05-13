#手写SpringMVC
    版本：v1
    简介：手写SpringMVC
    路径：com.spring.mvcframework.v1.servlet
    
    
## Spring思路
    1、调用Servlet init()方法，就需要初始化ApplicationContext
    2、读取配置文件(propertes、xml、yml)成功后；会生成BeanDefinition配置文件
    3、扫描相关的类；扫描每一个类会变成BeanDefinition并保存到内存中
    4、初始化IOC容器，并且实例化对象；BeanWrapper保存的是实例对象，BeanWrapper是由调用getBean()时创建的
    5、完成DI注入   


###ApplicationContext 简单的理解为它就是工厂类
    1、重要的方法：getBean()方法
       作用：从ioc容器中获取实例的方法
    2、Spring中发生DI由getBean()触发的，在调用getBean()创建对象时会触发DI发生

###Spring默认是单利模式，而且是延时加载(Lazy)的


##IOC中3个最重要的类
    1、BeanDefiniton
    2、BeanWrapper
    3、ApplicationContext
    简易流程图：
    BeanDefiniton-》BeanWrapper-》getBean()-》ApplicationContext-》BeanDeinitionReader-》BeanDefiniton