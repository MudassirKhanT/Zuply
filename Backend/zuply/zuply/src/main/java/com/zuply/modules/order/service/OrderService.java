package com.zuply.modules.order.service;

import com.zuply.modules.order.model.Order;
import com.zuply.modules.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> findByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }
}