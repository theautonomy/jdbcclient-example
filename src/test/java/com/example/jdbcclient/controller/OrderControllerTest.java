package com.example.jdbcclient.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.model.Order;
import com.example.jdbcclient.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private OrderService orderService;

    @Test
    void getAllOrders_shouldReturnOrderList() throws Exception {
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING"),
                        new Order(2L, 2L, null, new BigDecimal("200.00"), "COMPLETED"));
        when(orderService.getAllOrders()).thenReturn(mockOrders);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("COMPLETED"));

        verify(orderService).getAllOrders();
    }

    @Test
    void getOrderById_shouldReturnOrder_whenExists() throws Exception {
        Order mockOrder = new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING");
        when(orderService.getOrderById(1L)).thenReturn(Optional.of(mockOrder));

        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(100.00))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService).getOrderById(1L);
    }

    @Test
    void getOrderById_shouldReturn404_whenNotExists() throws Exception {
        when(orderService.getOrderById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/orders/999")).andExpect(status().isNotFound());

        verify(orderService).getOrderById(999L);
    }

    @Test
    void getOrdersByCustomerId_shouldReturnCustomerOrders() throws Exception {
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING"),
                        new Order(2L, 1L, null, new BigDecimal("200.00"), "COMPLETED"));
        when(orderService.getOrdersByCustomerId(1L)).thenReturn(mockOrders);

        mockMvc.perform(get("/api/orders/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].customerId").value(1));

        verify(orderService).getOrdersByCustomerId(1L);
    }

    @Test
    void getOrdersByStatus_shouldReturnOrdersWithStatus() throws Exception {
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("100.00"), "PENDING"),
                        new Order(2L, 2L, null, new BigDecimal("200.00"), "PENDING"));
        when(orderService.getOrdersByStatus("PENDING")).thenReturn(mockOrders);

        mockMvc.perform(get("/api/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(orderService).getOrdersByStatus("PENDING");
    }

    @Test
    void createOrder_shouldReturnCreatedStatus() throws Exception {
        Order newOrder = new Order(1L, new BigDecimal("150.00"), "PENDING");
        when(orderService.createOrder(any(Order.class))).thenReturn(10L);

        mockMvc.perform(
                        post("/api/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newOrder)))
                .andExpect(status().isCreated())
                .andExpect(content().string("10"));

        verify(orderService).createOrder(any(Order.class));
    }

    @Test
    void updateOrderStatus_shouldReturnOkStatus() throws Exception {
        mockMvc.perform(patch("/api/orders/1/status").param("status", "SHIPPED"))
                .andExpect(status().isOk());

        verify(orderService).updateOrderStatus(1L, "SHIPPED");
    }

    @Test
    void deleteOrder_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/orders/1")).andExpect(status().isNoContent());

        verify(orderService).deleteOrder(1L);
    }

    @Test
    void getTotalRevenue_shouldReturnRevenue() throws Exception {
        when(orderService.calculateTotalRevenue()).thenReturn(new BigDecimal("1500.00"));

        mockMvc.perform(get("/api/orders/revenue/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("1500.00"));

        verify(orderService).calculateTotalRevenue();
    }

    @Test
    void getOrderStatistics_shouldReturnStatistics() throws Exception {
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
        when(orderService.getOrderStatistics()).thenReturn(mockStats);

        mockMvc.perform(get("/api/orders/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOrders").value(10))
                .andExpect(jsonPath("$.uniqueCustomers").value(5));

        verify(orderService).getOrderStatistics();
    }

    @Test
    void getOrdersAboveAmount_shouldReturnFilteredOrders() throws Exception {
        List<Order> mockOrders =
                List.of(
                        new Order(1L, 1L, null, new BigDecimal("150.00"), "PENDING"),
                        new Order(2L, 2L, null, new BigDecimal("200.00"), "COMPLETED"));
        when(orderService.getOrdersAboveAmount(any(BigDecimal.class))).thenReturn(mockOrders);

        mockMvc.perform(get("/api/orders/above-amount").param("minAmount", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(orderService).getOrdersAboveAmount(any(BigDecimal.class));
    }

    @Test
    void countOrdersByCustomer_shouldReturnCount() throws Exception {
        when(orderService.countOrdersByCustomer(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/orders/count/customer/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(orderService).countOrdersByCustomer(1L);
    }
}
