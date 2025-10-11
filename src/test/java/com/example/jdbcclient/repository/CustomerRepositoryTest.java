package com.example.jdbcclient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import com.example.jdbcclient.dto.CustomerSummary;
import com.example.jdbcclient.model.Customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CustomerRepository.class)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class CustomerRepositoryTest {

    @Autowired private CustomerRepository customerRepository;

    @Test
    void findAll_shouldReturnAllCustomers() {
        List<Customer> customers = customerRepository.findAll();

        assertThat(customers).isNotEmpty();
        assertThat(customers).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void findById_shouldReturnCustomer_whenCustomerExists() {
        Optional<Customer> customer = customerRepository.findById(1L);

        assertThat(customer).isPresent();
        assertThat(customer.get().getName()).isEqualTo("John Doe");
        assertThat(customer.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findById_shouldReturnEmpty_whenCustomerDoesNotExist() {
        Optional<Customer> customer = customerRepository.findById(999L);

        assertThat(customer).isEmpty();
    }

    @Test
    void findByNameAndStatus_shouldReturnMatchingCustomers() {
        List<Customer> customers = customerRepository.findByNameAndStatus("John", "ACTIVE");

        assertThat(customers).isNotEmpty();
        assertThat(customers).allMatch(c -> c.getStatus().equals("ACTIVE"));
        assertThat(customers).anyMatch(c -> c.getName().contains("John"));
    }

    @Test
    void createCustomer_shouldInsertNewCustomer() {
        Customer newCustomer = new Customer("Test User", "test@example.com", "ACTIVE");

        Long customerId = customerRepository.createCustomer(newCustomer);

        assertThat(customerId).isNotNull().isPositive();

        Optional<Customer> savedCustomer = customerRepository.findById(customerId);
        assertThat(savedCustomer).isPresent();
        assertThat(savedCustomer.get().getName()).isEqualTo("Test User");
        assertThat(savedCustomer.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void updateCustomer_shouldModifyExistingCustomer() {
        Customer customer = customerRepository.findById(1L).orElseThrow();
        customer.setName("Updated Name");
        customer.setStatus("INACTIVE");

        int rowsUpdated = customerRepository.updateCustomer(customer);

        assertThat(rowsUpdated).isEqualTo(1);

        Customer updatedCustomer = customerRepository.findById(1L).orElseThrow();
        assertThat(updatedCustomer.getName()).isEqualTo("Updated Name");
        assertThat(updatedCustomer.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void deleteCustomer_shouldRemoveCustomer() {
        Customer newCustomer = new Customer("To Delete", "delete@example.com", "ACTIVE");
        Long customerId = customerRepository.createCustomer(newCustomer);

        int rowsDeleted = customerRepository.deleteCustomer(customerId);

        assertThat(rowsDeleted).isEqualTo(1);
        assertThat(customerRepository.findById(customerId)).isEmpty();
    }

    @Test
    void createCustomersBatch_shouldInsertMultipleCustomers() {
        List<Customer> customers =
                List.of(
                        new Customer("Batch 1", "batch1@example.com", "ACTIVE"),
                        new Customer("Batch 2", "batch2@example.com", "ACTIVE"),
                        new Customer("Batch 3", "batch3@example.com", "INACTIVE"));

        customerRepository.createCustomersBatch(customers);

        List<Customer> allCustomers = customerRepository.findAll();
        assertThat(allCustomers).hasSizeGreaterThanOrEqualTo(8); // 5 original + 3 new
    }

    @Test
    void findCustomerSummaries_shouldReturnCustomersWithOrderStatistics() {
        List<CustomerSummary> summaries = customerRepository.findCustomerSummaries();

        assertThat(summaries).isNotEmpty();
        assertThat(summaries)
                .allMatch(s -> s.id() != null && s.name() != null && s.email() != null);

        // John Doe should have orders
        CustomerSummary johnSummary =
                summaries.stream()
                        .filter(s -> s.name().equals("John Doe"))
                        .findFirst()
                        .orElseThrow();
        assertThat(johnSummary.orderCount()).isGreaterThan(0);
        assertThat(johnSummary.totalSpent()).isNotNull();
    }

    @Test
    void countByStatus_shouldReturnCorrectCount() {
        Long activeCount = customerRepository.countByStatus("ACTIVE");

        assertThat(activeCount).isGreaterThanOrEqualTo(4);
    }

    @Test
    void findCustomersWithNoOrders_shouldReturnCustomersWithoutOrders() {
        List<Customer> customersWithNoOrders = customerRepository.findCustomersWithNoOrders();

        assertThat(customersWithNoOrders).isNotEmpty();
        // Bob Johnson (id=3) should be in this list as he has INACTIVE status and no orders
        assertThat(customersWithNoOrders)
                .anyMatch(
                        c -> c.getName().equals("Bob Johnson") || c.getStatus().equals("INACTIVE"));
    }

    @Test
    void findByEmail_shouldReturnCustomer_whenEmailExists() {
        Optional<Customer> customer = customerRepository.findByEmail("john.doe@example.com");

        assertThat(customer).isPresent();
        assertThat(customer.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<Customer> customer = customerRepository.findByEmail("nonexistent@example.com");

        assertThat(customer).isEmpty();
    }

    @Test
    void findTopActiveCustomers_shouldReturnLimitedResults() {
        List<Customer> topCustomers = customerRepository.findTopActiveCustomers(3);

        assertThat(topCustomers).hasSizeLessThanOrEqualTo(3);
        assertThat(topCustomers).allMatch(c -> c.getStatus().equals("ACTIVE"));
    }
}
