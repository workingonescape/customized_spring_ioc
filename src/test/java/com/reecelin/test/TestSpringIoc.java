package com.reecelin.test;

import org.springframework.container.ClassPathXmlApplicationContext;

/**
 * @author Reece
 * @version 1.0.0
 * @ClassName TestSpringIoc.java
 * @Description TODO
 * @createTime 2021年05月09日 11:51:00
 */
public class TestSpringIoc {


    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

}
