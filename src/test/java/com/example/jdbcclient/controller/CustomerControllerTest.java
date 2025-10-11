package com.example.jdbcclient.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.jdbcclient.dto.CustomerSummary;
import com.example.jdbcclient.model.Customer;
import com.example.jdbcclient.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private CustomerService customerService;

    @Test
    void getAllCustomers_shouldReturnCustomerList() throws Exception {
        List<Customer> mockCustomers =
                List.of(
                        new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null),
                        new Customer(2L, "Jane Smith", "jane@example.com", "ACTIVE", null));
        when(customerService.getAllCustomers()).thenReturn(mockCustomers);

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));

        verify(customerService).getAllCustomers();
    }

    @Test
    void getCustomerById_shouldReturnCustomer_whenExists() throws Exception {
        Customer mockCustomer = new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null);
        when(customerService.getCustomerById(1L)).thenReturn(Optional.of(mockCustomer));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));

        verify(customerService).getCustomerById(1L);
    }

    @Test
    void getCustomerById_shouldReturn404_whenNotExists() throws Exception {
        when(customerService.getCustomerById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/customers/999")).andExpect(status().isNotFound());

        verify(customerService).getCustomerById(999L);
    }

    @Test
    void searchCustomers_shouldReturnMatchingCustomers() throws Exception {
        List<Customer> mockCustomers =
                List.of(new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null));
        when(customerService.searchCustomers("John", "ACTIVE")).thenReturn(mockCustomers);

        mockMvc.perform(
                        get("/api/customers/search")
                                .param("name", "John")
                                .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("John Doe"));

        verify(customerService).searchCustomers("John", "ACTIVE");
    }

    @Test
    void getCustomerByEmail_shouldReturnCustomer_whenExists() throws Exception {
        Customer mockCustomer = new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null);
        when(customerService.getCustomerByEmail("john@example.com"))
                .thenReturn(Optional.of(mockCustomer));

        mockMvc.perform(get("/api/customers/email/john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(customerService).getCustomerByEmail("john@example.com");
    }

    @Test
    void createCustomer_shouldReturnCreatedStatus() throws Exception {
        Customer newCustomer = new Customer("Test User", "test@example.com", "ACTIVE");
        when(customerService.createCustomer(any(Customer.class))).thenReturn(10L);

        mockMvc.perform(
                        post("/api/customers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newCustomer)))
                .andExpect(status().isCreated())
                .andExpect(content().string("10"));

        verify(customerService).createCustomer(any(Customer.class));
    }

    @Test
    void updateCustomer_shouldReturnOkStatus() throws Exception {
        Customer updatedCustomer =
                new Customer(1L, "Updated Name", "updated@example.com", "ACTIVE", null);

        mockMvc.perform(
                        put("/api/customers/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedCustomer)))
                .andExpect(status().isOk());

        verify(customerService).updateCustomer(any(Customer.class));
    }

    @Test
    void deleteCustomer_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/customers/1")).andExpect(status().isNoContent());

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    void createCustomersBatch_shouldReturnCreatedStatus() throws Exception {
        List<Customer> customers =
                List.of(
                        new Customer("User 1", "user1@example.com", "ACTIVE"),
                        new Customer("User 2", "user2@example.com", "ACTIVE"));

        mockMvc.perform(
                        post("/api/customers/batch")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(customers)))
                .andExpect(status().isCreated());

        verify(customerService).createCustomersBatch(any());
    }

    @Test
    void getCustomerSummaries_shouldReturnSummaryList() throws Exception {
        List<CustomerSummary> mockSummaries =
                List.of(
                        new CustomerSummary(
                                1L, "John Doe", "john@example.com", 2, new BigDecimal("500.00")),
                        new CustomerSummary(
                                2L, "Jane Smith", "jane@example.com", 1, new BigDecimal("200.00")));
        when(customerService.getCustomerSummaries()).thenReturn(mockSummaries);

        mockMvc.perform(get("/api/customers/summaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderCount").value(2));

        verify(customerService).getCustomerSummaries();
    }

    @Test
    void countActiveCustomers_shouldReturnCount() throws Exception {
        when(customerService.countActiveCustomers()).thenReturn(10L);

        mockMvc.perform(get("/api/customers/count/active"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));

        verify(customerService).countActiveCustomers();
    }

    @Test
    void getCustomersWithNoOrders_shouldReturnCustomerList() throws Exception {
        List<Customer> mockCustomers =
                List.of(new Customer(3L, "Bob Johnson", "bob@example.com", "INACTIVE", null));
        when(customerService.getCustomersWithNoOrders()).thenReturn(mockCustomers);

        mockMvc.perform(get("/api/customers/no-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(customerService).getCustomersWithNoOrders();
    }

    @Test
    void getTopActiveCustomers_shouldReturnLimitedCustomers() throws Exception {
        List<Customer> mockCustomers =
                List.of(
                        new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null),
                        new Customer(2L, "Jane Smith", "jane@example.com", "ACTIVE", null));
        when(customerService.getTopActiveCustomers(5)).thenReturn(mockCustomers);

        mockMvc.perform(get("/api/customers/top-active").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(customerService).getTopActiveCustomers(5);
    }
}
