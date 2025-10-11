package com.example.jdbcclient.repository;

import java.util.List;
import java.util.Optional;

import com.example.jdbcclient.dto.CustomerSummary;
import com.example.jdbcclient.model.Customer;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {

    private final JdbcClient jdbcClient;

    public CustomerRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /** Example 1: Simple query - Find all customers */
    public List<Customer> findAll() {
        return jdbcClient
                .sql("SELECT id, name, email, status, created_at FROM customers")
                .query(Customer.class)
                .list();
    }

    /** Example 2: Parameterized query - Find by ID */
    public Optional<Customer> findById(Long id) {
        return jdbcClient
                .sql("SELECT id, name, email, status, created_at FROM customers WHERE id = :id")
                .param("id", id)
                .query(Customer.class)
                .optional();
    }

    /** Example 3: Multiple parameters - Find by name and status */
    public List<Customer> findByNameAndStatus(String name, String status) {
        return jdbcClient
                .sql(
                        """
                SELECT id, name, email, status, created_at
                FROM customers
                WHERE name LIKE :name AND status = :status
                """)
                .param("name", "%" + name + "%")
                .param("status", status)
                .query(Customer.class)
                .list();
    }

    /** Example 4: Insert operation with named parameters */
    public Long createCustomer(Customer customer) {
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
        return key.longValue();
    }

    /** Example 5: Update operation */
    public int updateCustomer(Customer customer) {
        return jdbcClient
                .sql(
                        """
                UPDATE customers
                SET name = :name, email = :email, status = :status
                WHERE id = :id
                """)
                .param("name", customer.getName())
                .param("email", customer.getEmail())
                .param("status", customer.getStatus())
                .param("id", customer.getId())
                .update();
    }

    /** Example 6: Delete operation */
    public int deleteCustomer(Long id) {
        return jdbcClient.sql("DELETE FROM customers WHERE id = :id").param("id", id).update();
    }

    /** Example 7: Batch insert operation */
    public void createCustomersBatch(List<Customer> customers) {
        for (Customer customer : customers) {
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
        }
    }

    /** Example 8: Complex query with JOIN - Customer summary with order statistics */
    public List<CustomerSummary> findCustomerSummaries() {
        return jdbcClient
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
                ORDER BY totalSpent DESC
                """)
                .query(CustomerSummary.class)
                .list();
    }

    /** Example 9: Query single value - Count customers by status */
    public Long countByStatus(String status) {
        return jdbcClient
                .sql("SELECT COUNT(*) FROM customers WHERE status = :status")
                .param("status", status)
                .query(Long.class)
                .single();
    }

    /** Example 10: Find customers with no orders */
    public List<Customer> findCustomersWithNoOrders() {
        return jdbcClient
                .sql(
                        """
                SELECT c.id, c.name, c.email, c.status, c.created_at
                FROM customers c
                LEFT JOIN orders o ON c.id = o.customer_id
                WHERE o.id IS NULL
                """)
                .query(Customer.class)
                .list();
    }

    /** Example 11: Using params() method for bulk parameter binding */
    public Optional<Customer> findByEmail(String email) {
        return jdbcClient
                .sql(
                        """
                SELECT id, name, email, status, created_at
                FROM customers
                WHERE email = :email
                """)
                .params(java.util.Map.of("email", email))
                .query(Customer.class)
                .optional();
    }

    /** Example 12: Query with LIMIT */
    public List<Customer> findTopActiveCustomers(int limit) {
        return jdbcClient
                .sql(
                        """
                SELECT c.id, c.name, c.email, c.status, c.created_at
                FROM customers c
                LEFT JOIN orders o ON c.id = o.customer_id
                WHERE c.status = :status
                GROUP BY c.id, c.name, c.email, c.status, c.created_at
                ORDER BY COUNT(o.id) DESC
                LIMIT :limit
                """)
                .param("status", "ACTIVE")
                .param("limit", limit)
                .query(Customer.class)
                .list();
    }
}
