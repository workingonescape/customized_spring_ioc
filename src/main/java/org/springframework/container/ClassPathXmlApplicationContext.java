package org.springframework.container;

import org.springframework.annotation.ioc.Autowired;
import org.springframework.annotation.ioc.Component;
import org.springframework.annotation.ioc.Controller;
import org.springframework.annotation.ioc.Service;
import org.springframework.xml.SpringConfigParser;

import java.io.File;
import java.lang.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Reece
 * @version 1.0.0
 * @ClassName ClassPathXmlApplicationContext.java
 * @Description TODO
 * @createTime 2021年05月09日 10:23:00
 */
public class ClassPathXmlApplicationContext {


    //applicationContext.xml
    private String configBase;


    //存储类的全路径
    private final List<String> classPaths = new CopyOnWriteArrayList<>();


    //存储对象名与对象之间的映射 byName
    private Map<String, Object> iocNameContainer;


    //存储class与对象之间的映射 byType
    private Map<Class<?>, Object> iocContainer;


    //存储接口与对象之间的映射
    private Map<Class<?>, List<Object>> iocInterfaceContainer;


    public ClassPathXmlApplicationContext() {

    }

    public ClassPathXmlApplicationContext(String configBase) {
        this.configBase = configBase;
        init();
    }


    /**
     * @methodName：init
     * @description: 初始化方法
     * @param:
     * @return: void
     * @date: 2021-05-09 12:07:59
     */
    private void init() {
        String baePackage = SpringConfigParser.getBaePackage(configBase);
        //获取类的全路径
        findClassPath(baePackage);
        //实例化
        doInstance();
        //依赖注入 DI
        doDI();
    }


    /**
     * @methodName：loadClasses
     * @description: 加载类
     * @param: basePackage
     * @return: void
     * @date: 2021-05-09 12:24:03
     */
    private void findClassPath(String basePackage) {
        if (basePackage == null || basePackage.isEmpty()) {
            throw new RuntimeException("basePackage is null");
        }
        // basePackage: com.reecelin
        URL url = Thread.currentThread().getContextClassLoader().getResource("");

        //url: file:/E:/Idea_WorkSpace/customized_spring_ioc/classes/
        if (url == null) {
            throw new RuntimeException("url is null");
        }
        basePackage = basePackage.replace(".", File.separator);
        // file: E:\Idea_WorkSpace\customized_spring_ioc\target\classes\com\reecelin
        File file = new File(url.toString().replace("file:/", ""), basePackage);
        findAllClasses(file);
    }


    /**
     * @description: 递归遍历每个包，找出每个类的全路径
     * @param: file
     * @return: void
     * @date: 2021-05-10 14:29:28
     */
    private void findAllClasses(File file) {
        if (file == null) {
            throw new RuntimeException("file not exists");
        }
        File[] files = file.listFiles();
        if (files == null) {
            throw new RuntimeException("error occurred when finding classPath");
        }
        for (File f : files) {
            if (!f.isDirectory()) {
                //类的全路径
                String path = getFullPath(f.getPath());
                classPaths.add(path);
            } else {
                findAllClasses(f);
            }
        }
    }


    /**
     * @description: 获取类的全路径
     * @param: path
     * @return: java.lang.String
     * @date: 2021-05-09 12:34:18
     */
    private String getFullPath(String path) {
        int index = path.indexOf("classes\\");
        path = path.substring(index + 8, path.length() - 6);
        path = path.replace(File.separator, ".");
        return path;
    }


    /**
     * @description: 实例化并保存到IOC容器中
     * @param:
     * @return: void
     * @date: 2021-05-09 13:16:16
     */
    private void doInstance() {
        if (classPaths.isEmpty()) {
            return;
        }
        try {
            //懒加载
            iocNameContainer = new ConcurrentHashMap<>();
            iocContainer = new ConcurrentHashMap<>();
            iocInterfaceContainer = new ConcurrentHashMap<>();


            //遍历
            for (String classPath : classPaths) {

                Class<?> c = Class.forName(classPath);

                //接口不用进行实例化
                if (c.isInterface()) {
                    continue;
                }

                //有注解标注的类要获取类名或者别名
                String annotationName = null;

                //对象名 用作iocNameContainer的key
                String objectName = "";

                //实例化
                Object o = c.newInstance();

                //按类型注入时使用到
                iocContainer.put(c, o);

                //获取注解
                Component componentAnnotation = c.getAnnotation(Component.class);
                Service serviceAnnotation = c.getAnnotation(Service.class);
                Controller controllerAnnotation = c.getAnnotation(Controller.class);

                //判断类上是否有注解
                if (componentAnnotation != null || serviceAnnotation != null || controllerAnnotation != null) {


                    if (componentAnnotation != null || serviceAnnotation != null) {

                        //类实现的接口
                        Class<?>[] interfaces = c.getInterfaces();

                        //遍历实现的接口
                        for (Class<?> inter : interfaces) {
                            //获取对象集合
                            List<Object> objects = iocInterfaceContainer.get(inter);

                            if (objects == null) {
                                //初始化
                                objects = new CopyOnWriteArrayList<>();
                                //存放到list中
                                objects.add(o);
                                //接口与对象之间的映射
                                iocInterfaceContainer.put(inter, objects);
                            } else {
                                //直接存放到list中
                                objects.add(0);
                            }
                        }
                    }


                    //同一个类上三者只有一个
                    if (componentAnnotation != null) {
                        annotationName = componentAnnotation.value();
                    } else {
                        annotationName = controllerAnnotation == null ? serviceAnnotation.value() : controllerAnnotation.value();
                    }
                }

                if (annotationName == null || "".equals(annotationName)) {
                    String className = c.getSimpleName();
                    objectName = String.valueOf(className.charAt(0)).toLowerCase() + className.substring(1);
                } else {
                    objectName = annotationName;
                }

                //不能使用相同名称
                if (iocNameContainer.containsKey(objectName)) {
                    throw new RuntimeException("IOC has already exists bean name: " + objectName);
                }

                //按名字注入时使用到
                iocNameContainer.put(objectName, o);
            }
        } catch (ClassNotFoundException | InstantiationException |
                IllegalAccessException e) {
            e.printStackTrace();
        }

    }


    /**
     * @description: 依赖注入
     * @param:
     * @return: void
     * @date: 2021-05-09 17:19:26
     */
    private void doDI() {
        //使用iocContainer
        Set<Class<?>> classes = iocContainer.keySet();
        //需要进行依赖注入的类
        for (Class<?> clazz : classes) {
            //获取类的属性
            Field[] declaredFields = clazz.getDeclaredFields();
            //遍历属性
            for (Field field : declaredFields) {
                //属性上是否有Autowired注解
                if (field.isAnnotationPresent(Autowired.class)) {

                    //需要被注入的属性
                    Object bean = null;
                    //需要进行依赖注入
                    Autowired autowired = field.getAnnotation(Autowired.class);

                    //按照名称进行查找
                    if (!"".equals(autowired.value())) {
                        //按照名称查找
                        bean = getBean(autowired.value());
                        if (bean == null) {
                            throw new RuntimeException("No qualifying bean of " + autowired.value() + "  found");
                        }
                    } else {
                        //按照类型查找 这里使用的是filed属性的类型
                        Class<?> filedType = field.getType();
                        bean = getBean(filedType);

                        //类型查找失败时还要进行接口查找
                        if (bean == null) {
                            //按照接口来查找
                            List<Object> objects = getBeanByInterface(filedType);
                            if (objects == null) {
                                throw new RuntimeException("No qualifying bean of " + filedType + "  found");
                            } else if (objects.size() > 1) {
                                throw new RuntimeException("duplicate  bean of " + filedType + ", need 1, but found " + objects.size());
                            } else {
                                bean = objects.get(0);
                            }
                        }
                    }
                    field.setAccessible(Boolean.TRUE);
                    try {
                        //设置属性值，也就是依赖注入真正实现的地方
                        //iocContainer.get(clazz) 属性需要被注入的类
                        field.set(iocContainer.get(clazz), bean);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public Object getBean(String beanName) {
        return iocNameContainer.get(beanName);
    }

    public Object getBean(Class<?> clazz) {
        return iocContainer.get(clazz);
    }


    private List<Object> getBeanByInterface(Class<?> clazz) {
        return iocInterfaceContainer.get(clazz);
    }

}



