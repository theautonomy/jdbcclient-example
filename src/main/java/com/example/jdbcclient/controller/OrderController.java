package com.example.jdbcclient.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.example.jdbcclient.model.Order;
import com.example.jdbcclient.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService
                .getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody Order order) {
        Long orderId = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/revenue/total")
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        return ResponseEntity.ok(orderService.calculateTotalRevenue());
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getOrderStatistics() {
        return ResponseEntity.ok(orderService.getOrderStatistics());
    }

    @GetMapping("/above-amount")
    public ResponseEntity<List<Order>> getOrdersAboveAmount(@RequestParam BigDecimal minAmount) {
        return ResponseEntity.ok(orderService.getOrdersAboveAmount(minAmount));
    }

    @GetMapping("/count/customer/{customerId}")
    public ResponseEntity<Long> countOrdersByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.countOrdersByCustomer(customerId));
    }
}
