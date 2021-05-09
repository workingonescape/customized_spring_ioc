package com.reecelin.controller;

import com.reecelin.bean.Order;
import com.reecelin.service.OrderService;
import org.springframework.annotation.Autowired;
import org.springframework.annotation.Controller;

import java.util.List;

/**
 * @author Reece
 * @ClassName OrderController.java
 * @Description TODO
 * @createTime 2021年05月09日 12:39:00
 */
@Controller("orderController")
public class OrderController {


    @Autowired
    private OrderService orderService;


    public void getOrders() {
        List<Order> orders = orderService.findAllOrders();
        for (Order o : orders) {
            System.out.println(o);

        }
    }
}
