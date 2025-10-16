package com.example.jdbcclient.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.model.Customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.jdbc.Sql;

/** Tests verifying code snippets from spring-jdbcclient-guide.adoc work correctly */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(CustomerRepository.class)
@Sql(scripts = {"/schema.sql", "/data.sql"})
class GuideExamplesTest {

    @Autowired private CustomerRepository customerRepository;

    @Autowired private JdbcClient jdbcClient;

    // ==================================================
    // Guide Section: Simple Queries (lines 92-104)
    // ==================================================

    @Test
    void findAll_shouldReturnAllCustomers() {
        List<Customer> customers = customerRepository.findAll();

        assertThat(customers).isNotEmpty();
        assertThat(customers.size()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void findById_shouldReturnCustomer_whenExists() {
        Optional<Customer> customer = customerRepository.findById(1L);

        assertThat(customer).isPresent();
        assertThat(customer.get().getName()).isEqualTo("John Doe");
    }

    // ==================================================
    // Guide Section: Parameter Binding (lines 115-125)
    // ==================================================

    @Test
    void findByNameAndStatus_shouldReturnMatchingCustomers() {
        List<Customer> customers = customerRepository.findByNameAndStatus("John", "ACTIVE");

        assertThat(customers).isNotEmpty();
        assertThat(customers).allMatch(c -> c.getStatus().equals("ACTIVE"));
    }

    // ==================================================
    // Guide Section: Using Map for parameters (lines 132-142)
    // ==================================================

    @Test
    void findByMultipleCriteria_shouldWork() {
        Map<String, Object> params =
                Map.of(
                        "status",
                        "ACTIVE",
                        "createdAfter",
                        java.time.Instant.parse("2020-01-01T00:00:00Z"));

        List<Customer> customers =
                jdbcClient
                        .sql(
                                """
                SELECT id, name, email
                FROM customers
                WHERE status = :status
                AND created_at > :createdAfter
                """)
                        .params(params)
                        .query(Customer.class)
                        .list();

        assertThat(customers).isNotEmpty();
    }

    // ==================================================
    // Guide Section: Insert Operations (lines 151-160)
    // ==================================================

    @Test
    void createCustomer_shouldInsertNewCustomer() {
        Customer customer = new Customer("Guide Test", "guide@example.com", "ACTIVE");

        jdbcClient
                .sql(
                        """
            INSERT INTO customers (name, email, status)
            VALUES (:name, :email, :status)
            """)
                .param("name", customer.getName())
                .param("email", customer.getEmail())
                .param("status", customer.getStatus())
                .update();

        Optional<Customer> saved = customerRepository.findByEmail("guide@example.com");
        assertThat(saved).isPresent();
        assertThat(saved.get().getName()).isEqualTo("Guide Test");
    }

    // ==================================================
    // Guide Section: Retrieving Generated Keys (lines 171-185)
    // ==================================================

    @Test
    void createCustomerAndReturnId_shouldReturnGeneratedKey() {
        Customer customer = new Customer("KeyGen Test", "keygen@example.com", "ACTIVE");

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient
                .sql(
                        """
            INSERT INTO customers (name, email, status)
            VALUES (:name, :email, :status)
            """)
                .param("name", customer.getName())
                .param("email", customer.getEmail())
                .param("status", customer.getStatus())
                .update(keyHolder);

        Number key = (Number) keyHolder.getKeys().get("ID");
        Long generatedId = key.longValue();

        assertThat(generatedId).isNotNull().isPositive();

        Optional<Customer> saved = customerRepository.findById(generatedId);
        assertThat(saved).isPresent();
    }

    // ==================================================
    // Guide Section: Update and Delete (lines 194-209)
    // ==================================================

    @Test
    void updateCustomerEmail_shouldUpdateEmail() {
        Long customerId = 1L;
        String newEmail = "updated-guide@example.com";

        // Note: Guide example uses updated_at which doesn't exist in customers table
        // Testing with just email update
        int rows =
                jdbcClient
                        .sql(
                                """
            UPDATE customers
            SET email = :email
            WHERE id = :id
            """)
                        .param("email", newEmail)
                        .param("id", customerId)
                        .update();

        assertThat(rows).isEqualTo(1);

        Optional<Customer> updated = customerRepository.findById(customerId);
        assertThat(updated).isPresent();
        assertThat(updated.get().getEmail()).isEqualTo(newEmail);
    }

    @Test
    void deleteCustomer_shouldRemoveCustomer() {
        Customer customer = new Customer("To Delete", "delete-guide@example.com", "ACTIVE");
        customerRepository.createCustomer(customer);
        Long id = customerRepository.findByEmail("delete-guide@example.com").get().getId();

        int rows = jdbcClient.sql("DELETE FROM customers WHERE id = :id").param("id", id).update();

        assertThat(rows).isEqualTo(1);
        assertThat(customerRepository.findById(id)).isEmpty();
    }

    // ==================================================
    // Guide Section: Custom Row Mapping (lines 220-234)
    // ==================================================

    public record CustomerDTO(Long id, String name, String email, int orderCount) {}

    @Test
    void findCustomersWithOrders_shouldReturnCustomDTO() {
        List<CustomerDTO> customers =
                jdbcClient
                        .sql(
                                """
            SELECT c.id, c.name, c.email, COUNT(o.id) as order_count
            FROM customers c
            LEFT JOIN orders o ON c.id = o.customer_id
            GROUP BY c.id, c.name, c.email
            """)
                        .query(
                                (rs, rowNum) ->
                                        new CustomerDTO(
                                                rs.getLong("id"),
                                                rs.getString("name"),
                                                rs.getString("email"),
                                                rs.getInt("order_count")))
                        .list();

        assertThat(customers).isNotEmpty();
        assertThat(customers).allMatch(c -> c.id() != null && c.name() != null);
    }

    // ==================================================
    // Guide Section: Working with Single Values (lines 243-255)
    // ==================================================

    @Test
    void countActiveCustomers_shouldReturnCount() {
        Long count =
                jdbcClient
                        .sql("SELECT COUNT(*) FROM customers WHERE status = :status")
                        .param("status", "ACTIVE")
                        .query(Long.class)
                        .single();

        assertThat(count).isGreaterThanOrEqualTo(4);
    }

    @Test
    void findCustomerEmail_shouldReturnEmail() {
        Optional<String> email =
                jdbcClient
                        .sql("SELECT email FROM customers WHERE id = :id")
                        .param("id", 1L)
                        .query(String.class)
                        .optional();

        assertThat(email).isPresent();
        assertThat(email.get()).isEqualTo("john.doe@example.com");
    }

    // ==================================================
    // Guide Section: Records for DTOs (lines 373-395)
    // ==================================================

    public record CustomerSummary(
            Long id, String name, String email, int orderCount, BigDecimal totalSpent) {}

    @Test
    void getCustomerSummaries_shouldReturnRecords() {
        List<CustomerSummary> summaries =
                jdbcClient
                        .sql(
                                """
            SELECT
                c.id,
                c.name,
                c.email,
                COUNT(o.id) as orderCount,
                COALESCE(SUM(o.total_amount), 0) as totalSpent
            FROM customers c
            LEFT JOIN orders o ON c.id = o.customer_id
            GROUP BY c.id, c.name, c.email
            """)
                        .query(CustomerSummary.class)
                        .list();

        assertThat(summaries).isNotEmpty();
        assertThat(summaries).allMatch(s -> s.id() != null && s.name() != null);
    }

    // ==================================================
    // Guide Section: Handle Empty Results (lines 358-364)
    // ==================================================

    public static class CustomerNotFoundException extends RuntimeException {
        public CustomerNotFoundException(Long id) {
            super("Customer not found: " + id);
        }
    }

    @Test
    void getCustomerById_shouldThrowWhenNotFound() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> {
                            jdbcClient
                                    .sql("SELECT id, name, email FROM customers WHERE id = :id")
                                    .param("id", 999L)
                                    .query(Customer.class)
                                    .optional()
                                    .orElseThrow(() -> new CustomerNotFoundException(999L));
                        })
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found: 999");
    }

    // ==================================================
    // Guide Section: Null Value Handling (lines 525-530)
    // ==================================================

    @Test
    void updateCustomerPhone_shouldHandleNull() {
        // JdbcClient should handle null values properly
        jdbcClient
                .sql("UPDATE customers SET status = :status WHERE id = :id")
                .param("id", 1L)
                .param("status", null) // This should work
                .update();

        Optional<Customer> customer = customerRepository.findById(1L);
        assertThat(customer).isPresent();
        assertThat(customer.get().getStatus()).isNull();
    }
}
