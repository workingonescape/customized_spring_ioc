package com.reecelin.service.impl;

import com.reecelin.bean.Order;
import com.reecelin.service.OrderService;
import org.springframework.annotation.ioc.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Reece
 * @ClassName OrderServiceImpl.java
 * @Description TODO
 * @createTime 2021年05月09日 12:48:00
 */
@Service("os1")
public class OrderServiceImpl implements OrderService {


    private List<Order> orders;

    public OrderServiceImpl() {
        orders = new ArrayList<>();
        Order o1 = new Order("test1", "iphone", "苹果手机", new BigDecimal("12999.00"), "上海", "12345");
        Order o2 = new Order("test2", "iphone", "苹果手机", new BigDecimal("12999.00"), "上海", "12345");
        Order o3 = new Order("test3", "iphone", "苹果手机", new BigDecimal("12999.00"), "上海", "12345");
        Order o4 = new Order("test4", "iphone", "苹果手机", new BigDecimal("12999.00"), "上海", "12345");
        Order o5 = new Order("test5", "iphone", "苹果手机", new BigDecimal("12999.00"), "上海", "12345");
        orders.add(o1);
        orders.add(o2);
        orders.add(o3);
        orders.add(o4);
        orders.add(o5);
    }

    /**
    * @description: 根据用户id查询订单
    * @param: id 用户id
    * @return: java.util.List<com.reecelin.bean.Order>
    * @date: 2021-05-09 15:23:12
    */
    @Override
    public List<Order> findAllOrders() {

        return orders ;
    }

}
