package com.example.jdbcclient.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.jdbcclient.dto.CustomerSummary;
import com.example.jdbcclient.model.Customer;
import com.example.jdbcclient.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository customerRepository;

    @InjectMocks private CustomerService customerService;

    @Test
    void getAllCustomers_shouldReturnAllCustomers() {
        List<Customer> mockCustomers =
                List.of(
                        new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null),
                        new Customer(2L, "Jane Smith", "jane@example.com", "ACTIVE", null));
        when(customerRepository.findAll()).thenReturn(mockCustomers);

        List<Customer> customers = customerService.getAllCustomers();

        assertThat(customers).hasSize(2);
        verify(customerRepository).findAll();
    }

    @Test
    void getCustomerById_shouldReturnCustomer_whenExists() {
        Customer mockCustomer = new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(mockCustomer));

        Optional<Customer> customer = customerService.getCustomerById(1L);

        assertThat(customer).isPresent();
        assertThat(customer.get().getName()).isEqualTo("John Doe");
        verify(customerRepository).findById(1L);
    }

    @Test
    void searchCustomers_shouldReturnMatchingCustomers() {
        List<Customer> mockCustomers =
                List.of(new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null));
        when(customerRepository.findByNameAndStatus("John", "ACTIVE")).thenReturn(mockCustomers);

        List<Customer> customers = customerService.searchCustomers("John", "ACTIVE");

        assertThat(customers).hasSize(1);
        verify(customerRepository).findByNameAndStatus("John", "ACTIVE");
    }

    @Test
    void createCustomer_shouldReturnNewCustomerId() {
        Customer newCustomer = new Customer("Test User", "test@example.com", "ACTIVE");
        when(customerRepository.createCustomer(any(Customer.class))).thenReturn(10L);

        Long customerId = customerService.createCustomer(newCustomer);

        assertThat(customerId).isEqualTo(10L);
        verify(customerRepository).createCustomer(newCustomer);
    }

    @Test
    void updateCustomer_shouldCallRepository() {
        Customer customer = new Customer(1L, "Updated Name", "updated@example.com", "ACTIVE", null);

        customerService.updateCustomer(customer);

        verify(customerRepository).updateCustomer(customer);
    }

    @Test
    void deleteCustomer_shouldCallRepository() {
        customerService.deleteCustomer(1L);

        verify(customerRepository).deleteCustomer(1L);
    }

    @Test
    void createCustomersBatch_shouldCallRepository() {
        List<Customer> customers =
                List.of(
                        new Customer("User 1", "user1@example.com", "ACTIVE"),
                        new Customer("User 2", "user2@example.com", "ACTIVE"));

        customerService.createCustomersBatch(customers);

        verify(customerRepository).createCustomersBatch(customers);
    }

    @Test
    void getCustomerSummaries_shouldReturnSummaries() {
        List<CustomerSummary> mockSummaries =
                List.of(
                        new CustomerSummary(
                                1L, "John Doe", "john@example.com", 2, new BigDecimal("500.00")),
                        new CustomerSummary(
                                2L, "Jane Smith", "jane@example.com", 1, new BigDecimal("200.00")));
        when(customerRepository.findCustomerSummaries()).thenReturn(mockSummaries);

        List<CustomerSummary> summaries = customerService.getCustomerSummaries();

        assertThat(summaries).hasSize(2);
        verify(customerRepository).findCustomerSummaries();
    }

    @Test
    void countActiveCustomers_shouldReturnCount() {
        when(customerRepository.countByStatus("ACTIVE")).thenReturn(10L);

        Long count = customerService.countActiveCustomers();

        assertThat(count).isEqualTo(10L);
        verify(customerRepository).countByStatus("ACTIVE");
    }

    @Test
    void getCustomersWithNoOrders_shouldReturnCustomers() {
        List<Customer> mockCustomers =
                List.of(new Customer(3L, "Bob Johnson", "bob@example.com", "INACTIVE", null));
        when(customerRepository.findCustomersWithNoOrders()).thenReturn(mockCustomers);

        List<Customer> customers = customerService.getCustomersWithNoOrders();

        assertThat(customers).hasSize(1);
        verify(customerRepository).findCustomersWithNoOrders();
    }

    @Test
    void getCustomerByEmail_shouldReturnCustomer_whenExists() {
        Customer mockCustomer = new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null);
        when(customerRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(mockCustomer));

        Optional<Customer> customer = customerService.getCustomerByEmail("john@example.com");

        assertThat(customer).isPresent();
        assertThat(customer.get().getEmail()).isEqualTo("john@example.com");
        verify(customerRepository).findByEmail("john@example.com");
    }

    @Test
    void getTopActiveCustomers_shouldReturnLimitedCustomers() {
        List<Customer> mockCustomers =
                List.of(
                        new Customer(1L, "John Doe", "john@example.com", "ACTIVE", null),
                        new Customer(2L, "Jane Smith", "jane@example.com", "ACTIVE", null));
        when(customerRepository.findTopActiveCustomers(5)).thenReturn(mockCustomers);

        List<Customer> customers = customerService.getTopActiveCustomers(5);

        assertThat(customers).hasSize(2);
        verify(customerRepository).findTopActiveCustomers(5);
    }
}
