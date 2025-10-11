package com.example.jdbcclient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.model.Order;
import com.example.jdbcclient.repository.OrderRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;

    @InjectMocks private OrderService orderService;

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING"),
                        new Order(2L, 2L, null, new BigDecimal("200.00"), "COMPLETED"));
        when(orderRepository.findAll()).thenReturn(mockOrders);

        List<Order> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(2);
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() {
        Order mockOrder = new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        Optional<Order> order = orderService.getOrderById(1L);

        assertThat(order).isPresent();
        assertThat(order.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrdersByCustomerId_shouldReturnCustomerOrders() {
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING"),
                        new Order(2L, 1L, null, new BigDecimal("200.00"), "COMPLETED"));
        when(orderRepository.findByCustomerId(1L)).thenReturn(mockOrders);

        List<Order> orders = orderService.getOrdersByCustomerId(1L);

        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getCustomerId().equals(1L));
        verify(orderRepository).findByCustomerId(1L);
    }

    @Test
    void createOrder_shouldReturnNewOrderId() {
        Order newOrder = new Order(1L, new BigDecimal("150.00"), "PENDING");
        when(orderRepository.createOrder(any(Order.class))).thenReturn(10L);

        Long orderId = orderService.createOrder(newOrder);

        assertThat(orderId).isEqualTo(10L);
        verify(orderRepository).createOrder(newOrder);
    }

    @Test
    void updateOrderStatus_shouldCallRepository() {
        orderService.updateOrderStatus(1L, "SHIPPED");

        verify(orderRepository).updateOrderStatus(1L, "SHIPPED");
    }

    @Test
    void getOrdersByStatus_shouldReturnOrdersWithStatus() {
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING"),
                        new Order(2L, 2L, null, new BigDecimal("200.00"), "PENDING"));
        when(orderRepository.findByStatus("PENDING")).thenReturn(mockOrders);

        List<Order> orders = orderService.getOrdersByStatus("PENDING");

        assertThat(orders).hasSize(2);
        assertThat(orders).allMatch(o -> o.getStatus().equals("PENDING"));
        verify(orderRepository).findByStatus("PENDING");
    }

    @Test
    void calculateTotalRevenue_shouldReturnRevenue() {
        when(orderRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("1500.00"));

        BigDecimal revenue = orderService.calculateTotalRevenue();

        assertThat(revenue).isEqualByComparingTo(new BigDecimal("1500.00"));
        verify(orderRepository).calculateTotalRevenue();
    }

    @Test
    void getOrderStatistics_shouldReturnStatistics() {
        Map<String, Object> mockStats =
                Map.of(
                        "totalOrders",
                        10L,
                        "totalRevenue",
                        new BigDecimal("1500.00"),
                        "averageOrderValue",
                        new BigDecimal("150.00"),
                        "uniqueCustomers",
                        5L);
        when(orderRepository.getOrderStatistics()).thenReturn(mockStats);

        Map<String, Object> stats = orderService.getOrderStatistics();

        assertThat(stats).hasSize(4);
        assertThat(stats.get("totalOrders")).isEqualTo(10L);
        verify(orderRepository).getOrderStatistics();
    }

    @Test
    void getOrdersAboveAmount_shouldReturnOrdersAboveThreshold() {
        BigDecimal threshold = new BigDecimal("100.00");
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("150.00"), "PENDING"),
                        new Order(2L, 2L, null, new BigDecimal("200.00"), "COMPLETED"));
        when(orderRepository.findOrdersAboveAmount(threshold)).thenReturn(mockOrders);

        List<Order> orders = orderService.getOrdersAboveAmount(threshold);

        assertThat(orders).hasSize(2);
        verify(orderRepository).findOrdersAboveAmount(threshold);
    }

    @Test
    void deleteOrder_shouldCallRepository() {
        orderService.deleteOrder(1L);

        verify(orderRepository).deleteOrder(1L);
    }

    @Test
    void countOrdersByCustomer_shouldReturnCount() {
        when(orderRepository.countOrdersByCustomer(1L)).thenReturn(5L);

        Long count = orderService.countOrdersByCustomer(1L);

        assertThat(count).isEqualTo(5L);
        verify(orderRepository).countOrdersByCustomer(1L);
    }
}
