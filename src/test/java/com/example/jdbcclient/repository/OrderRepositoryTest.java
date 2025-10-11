package com.example.jdbcclient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.model.Order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OrderRepository.class)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;

    @Test
    void findAll_shouldReturnAllOrders() {
        List<Order> orders = orderRepository.findAll();

        assertThat(orders).isNotEmpty();
        assertThat(orders).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void findById_shouldReturnOrder_whenOrderExists() {
        Optional<Order> order = orderRepository.findById(1L);

        assertThat(order).isPresent();
        assertThat(order.get().getCustomerId()).isEqualTo(1L);
        assertThat(order.get().getTotalAmount()).isNotNull();
    }

    @Test
    void findById_shouldReturnEmpty_whenOrderDoesNotExist() {
        Optional<Order> order = orderRepository.findById(999L);

        assertThat(order).isEmpty();
    }

    @Test
    void findByCustomerId_shouldReturnCustomerOrders() {
        List<Order> orders = orderRepository.findByCustomerId(1L);

        assertThat(orders).isNotEmpty();
        assertThat(orders).allMatch(o -> o.getCustomerId().equals(1L));
    }

    @Test
    void createOrder_shouldInsertNewOrder() {
        Order newOrder = new Order(1L, new BigDecimal("500.00"), "PENDING");

        Long orderId = orderRepository.createOrder(newOrder);

        assertThat(orderId).isNotNull().isPositive();

        Optional<Order> savedOrder = orderRepository.findById(orderId);
        assertThat(savedOrder).isPresent();
        assertThat(savedOrder.get().getCustomerId()).isEqualTo(1L);
        assertThat(savedOrder.get().getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(savedOrder.get().getStatus()).isEqualTo("PENDING");
    }

    @Test
    void updateOrderStatus_shouldModifyOrderStatus() {
        int rowsUpdated = orderRepository.updateOrderStatus(1L, "SHIPPED");

        assertThat(rowsUpdated).isEqualTo(1);

        Order updatedOrder = orderRepository.findById(1L).orElseThrow();
        assertThat(updatedOrder.getStatus()).isEqualTo("SHIPPED");
    }

    @Test
    void findByStatus_shouldReturnOrdersWithMatchingStatus() {
        List<Order> pendingOrders = orderRepository.findByStatus("PENDING");

        assertThat(pendingOrders).isNotEmpty();
        assertThat(pendingOrders).allMatch(o -> o.getStatus().equals("PENDING"));
    }

    @Test
    void calculateTotalRevenue_shouldReturnSumOfCompletedOrders() {
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();

        assertThat(totalRevenue).isNotNull();
        assertThat(totalRevenue).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getOrderStatistics_shouldReturnStatisticsMap() {
        Map<String, Object> stats = orderRepository.getOrderStatistics();

        assertThat(stats).isNotNull();
        assertThat(stats)
                .containsKeys(
                        "totalOrders", "totalRevenue", "averageOrderValue", "uniqueCustomers");

        Long totalOrders = (Long) stats.get("totalOrders");
        BigDecimal totalRevenue = (BigDecimal) stats.get("totalRevenue");
        BigDecimal averageOrderValue = (BigDecimal) stats.get("averageOrderValue");
        Long uniqueCustomers = (Long) stats.get("uniqueCustomers");

        assertThat(totalOrders).isGreaterThanOrEqualTo(5L);
        assertThat(totalRevenue).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(averageOrderValue).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(uniqueCustomers).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void findOrdersAboveAmount_shouldReturnOrdersAboveThreshold() {
        BigDecimal threshold = new BigDecimal("100.00");
        List<Order> orders = orderRepository.findOrdersAboveAmount(threshold);

        assertThat(orders).isNotEmpty();
        assertThat(orders).allMatch(o -> o.getTotalAmount().compareTo(threshold) > 0);
    }

    @Test
    void findOrdersAboveAmount_shouldReturnEmptyList_whenNoOrdersAboveThreshold() {
        BigDecimal highThreshold = new BigDecimal("999999.00");
        List<Order> orders = orderRepository.findOrdersAboveAmount(highThreshold);

        assertThat(orders).isEmpty();
    }

    @Test
    void deleteOrder_shouldRemoveOrder() {
        Order newOrder = new Order(1L, new BigDecimal("100.00"), "PENDING");
        Long orderId = orderRepository.createOrder(newOrder);

        int rowsDeleted = orderRepository.deleteOrder(orderId);

        assertThat(rowsDeleted).isEqualTo(1);
        assertThat(orderRepository.findById(orderId)).isEmpty();
    }

    @Test
    void countOrdersByCustomer_shouldReturnCorrectCount() {
        Long orderCount = orderRepository.countOrdersByCustomer(1L);

        assertThat(orderCount).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void countOrdersByCustomer_shouldReturnZero_whenCustomerHasNoOrders() {
        // Customer 3 (Bob Johnson) has no orders
        Long orderCount = orderRepository.countOrdersByCustomer(3L);

        assertThat(orderCount).isEqualTo(0L);
    }
}
