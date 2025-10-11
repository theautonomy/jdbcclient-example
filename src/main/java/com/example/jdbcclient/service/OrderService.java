package com.example.jdbcclient.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.model.Order;
import com.example.jdbcclient.repository.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Long createOrder(Order order) {
        return orderRepository.createOrder(order);
    }

    public void updateOrderStatus(Long orderId, String status) {
        orderRepository.updateOrderStatus(orderId, status);
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public BigDecimal calculateTotalRevenue() {
        return orderRepository.calculateTotalRevenue();
    }

    public Map<String, Object> getOrderStatistics() {
        return orderRepository.getOrderStatistics();
    }

    public List<Order> getOrdersAboveAmount(BigDecimal minAmount) {
        return orderRepository.findOrdersAboveAmount(minAmount);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteOrder(id);
    }

    public Long countOrdersByCustomer(Long customerId) {
        return orderRepository.countOrdersByCustomer(customerId);
    }
}
