package com.reecelin.test;

import com.reecelin.controller.OrderController;
import org.springframework.container.ClassPathXmlApplicationContext;

/**
 * @author Reece
 * @ClassName TestSpring.java
 * @Description TODO
 * @createTime 2021年05月09日 12:14:00
 */
public class TestSpring {


    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        OrderController orderController = (OrderController) context.getBean("orderController");
        orderController.getOrders();
    }
}
