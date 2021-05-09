package org.springframework.container;

import javafx.beans.binding.ObjectExpression;
import org.springframework.annotation.Autowired;
import org.springframework.annotation.Controller;
import org.springframework.annotation.Service;
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


    //存储类全路径
    private List<String> classPaths = new ArrayList<>();


    //存储对象名与对象之间的映射 byName
    private Map<String, Object> iocNameContainer;


    //存储class与对象之间的映射 byType
    private Map<Class<?>, Object> iocContainer;


    //存储接口与对象之间的映射 主要用于service层
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
        if (basePackage == null) {
            throw new RuntimeException("basePackage is null");
        }
        //com.reecelin
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url == null) {
            throw new RuntimeException("url is null");
        }
        basePackage = basePackage.replace(".", File.separator);
        File file = new File(url.toString().replace("file:/", ""), basePackage);
        findAllClasses(file);
    }


    private void findAllClasses(File file) {
        if (file == null) {
            throw new RuntimeException("file not exists");
        }
        File[] files = file.listFiles();
        for (File f : files) {
            if (!f.isDirectory()) {
                //类的全路径
                String path = pathHandler(f.getPath());
                classPaths.add(path);
            } else {
                findAllClasses(f);
            }
        }
    }


    /**
     * @description: 处理path路径
     * @param: path
     * @return: java.lang.String
     * @date: 2021-05-09 12:34:18
     */
    private String pathHandler(String path) {
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
        if (classPaths.size() == 0) {
            return;
        }
        try {
            //懒加载
            iocNameContainer = new ConcurrentHashMap<>();
            iocContainer = new ConcurrentHashMap<>();
            iocInterfaceContainer = new ConcurrentHashMap<>();
            for (String classPath : classPaths) {
                Class<?> c = Class.forName(classPath);
                String objectName = "";
                if (c.isAnnotationPresent(Controller.class)) {
                    //实例化
                    Object o = c.newInstance();

                    Controller controllerAnnotation = c.getAnnotation(Controller.class);
                    //按类型注入时使用到
                    iocContainer.put(c, o);
                    if ("".equals(controllerAnnotation.value())) {
                        String className = c.getSimpleName();
                        objectName = String.valueOf(className.charAt(0)).toLowerCase() + className.substring(1);
                    } else {
                        objectName = controllerAnnotation.value();
                    }
                    if (iocNameContainer.containsKey(objectName)){
                        throw new RuntimeException("IOC has already exists" + objectName);
                    }
                    //按名称注入时使用
                    iocNameContainer.put(objectName, o);

                } else if (c.isAnnotationPresent(Service.class)) {
                    Object o = c.newInstance();
                    //获取接口
                    Class<?>[] interfaces = c.getInterfaces();

                    for (Class<?> inter : interfaces) {
                        List<Object> objects = iocInterfaceContainer.get(inter);
                        if (objects == null) {
                            objects = new ArrayList<>();
                            objects.add(o);
                            iocInterfaceContainer.put(inter, objects);
                        }else {
                            objects.add(0);
                        }
                    }

                    Service serviceAnnotation = c.getAnnotation(Service.class);

                    //按类型注入时使用到
                    iocContainer.put(c, o);

                    if ("".equals(serviceAnnotation.value())) {
                        String className = c.getSimpleName();
                        objectName = String.valueOf(className.charAt(0)).toLowerCase() + className.substring(1);
                    } else {
                        objectName = serviceAnnotation.value();
                    }

                    if (iocNameContainer.containsKey(objectName)){
                        throw new RuntimeException("IOC has already exists bean name: " + objectName);
                    }
                    //按名字注入时使用到
                    iocNameContainer.put(objectName, o);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    
    /**
    * @description: 属性注入
    * @param: 
    * @return: void
    * @date: 2021-05-09 17:19:26
    */
    private void doDI() {
        //使用iocContainer
        Set<Class<?>> classes = iocContainer.keySet();
        for (Class<?> aClass : classes) {
            Field[] declaredFields = aClass.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    Object bean = null;
                    //需要进行依赖注入
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    if (!"".equals(autowired.value())) {
                        //按照名称查找
                        bean = getBean(autowired.value());
                        if (bean == null) {
                            throw new RuntimeException("No qualifying bean of " + autowired.value() + "  found");
                        }
                    }else {
                        //按照类型查找 这里使用的是filed属性的类型
                        Class<?> filedType = field.getType();
                        bean = getBean(filedType);
                        if (bean == null) {
                            //按照接口来查找
                            List<Object> objects = getBeanByInterface(filedType);
                            if (objects == null) {
                                throw new RuntimeException("No qualifying bean of " + filedType + "  found");
                            } else if (objects.size() > 1) {
                                throw new RuntimeException("duplicate  bean of " + filedType + ", need 1, but found " + objects.size());
                            }else {
                                bean = objects.get(0);
                            }
                        }
                    }
                    field.setAccessible(Boolean.TRUE);
                    try {
                        //设置属性值，也就是依赖注入真正实现的地方
                        field.set(iocContainer.get(aClass), bean);
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

    public Object getBean(Class<?> clazz){
        return iocContainer.get(clazz);
    }


    private List<Object> getBeanByInterface(Class<?> clazz) {
        return iocInterfaceContainer.get(clazz);
    }

}



