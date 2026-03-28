package com.example.order;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderJdbcDao orderJdbcDao;

    public OrderService(OrderRepository orderRepository, OrderJdbcDao orderJdbcDao) {
        this.orderRepository = orderRepository;
        this.orderJdbcDao = orderJdbcDao;
    }

    public List<Object> listByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public void changeStatus(Long id, String status) {
        orderJdbcDao.updateOrderStatus(id, status);
    }
}