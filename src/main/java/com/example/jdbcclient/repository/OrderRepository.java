package com.example.jdbcclient.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.jdbcclient.model.Order;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {

    private final JdbcClient jdbcClient;

    public OrderRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /** Find all orders */
    public List<Order> findAll() {
        return jdbcClient
                .sql(
                        """
                SELECT id, customer_id, order_date, total_amount, status
                FROM orders
                ORDER BY order_date DESC
                """)
                .query(Order.class)
                .list();
    }

    /** Find order by ID */
    public Optional<Order> findById(Long id) {
        return jdbcClient
                .sql(
                        """
                SELECT id, customer_id, order_date, total_amount, status
                FROM orders
                WHERE id = :id
                """)
                .param("id", id)
                .query(Order.class)
                .optional();
    }

    /** Find orders by customer ID */
    public List<Order> findByCustomerId(Long customerId) {
        return jdbcClient
                .sql(
                        """
                SELECT id, customer_id, order_date, total_amount, status
                FROM orders
                WHERE customer_id = :customerId
                ORDER BY order_date DESC
                """)
                .param("customerId", customerId)
                .query(Order.class)
                .list();
    }

    /** Create new order */
    public Long createOrder(Order order) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcClient
                .sql(
                        """
                INSERT INTO orders (customer_id, total_amount, status)
                VALUES (:customerId, :totalAmount, :status)
                """)
                .param("customerId", order.getCustomerId())
                .param("totalAmount", order.getTotalAmount())
                .param("status", order.getStatus())
                .update(keyHolder);

        Number key = (Number) keyHolder.getKeys().get("ID");
        return key.longValue();
    }

    /** Update order status */
    public int updateOrderStatus(Long orderId, String status) {
        return jdbcClient
                .sql(
                        """
                UPDATE orders
                SET status = :status
                WHERE id = :orderId
                """)
                .param("status", status)
                .param("orderId", orderId)
                .update();
    }

    /** Find orders by status */
    public List<Order> findByStatus(String status) {
        return jdbcClient
                .sql(
                        """
                SELECT id, customer_id, order_date, total_amount, status
                FROM orders
                WHERE status = :status
                ORDER BY order_date DESC
                """)
                .param("status", status)
                .query(Order.class)
                .list();
    }

    /** Calculate total revenue - returns a single BigDecimal value */
    public BigDecimal calculateTotalRevenue() {
        return jdbcClient
                .sql(
                        """
                SELECT COALESCE(SUM(total_amount), 0)
                FROM orders
                WHERE status = 'COMPLETED'
                """)
                .query(BigDecimal.class)
                .single();
    }

    /** Get order statistics - using custom row mapper */
    public Map<String, Object> getOrderStatistics() {
        return jdbcClient
                .sql(
                        """
                SELECT
                    COUNT(*) as totalOrders,
                    COALESCE(SUM(total_amount), 0) as totalRevenue,
                    COALESCE(AVG(total_amount), 0) as averageOrderValue,
                    COUNT(DISTINCT customer_id) as uniqueCustomers
                FROM orders
                """)
                .query(
                        (rs, rowNum) -> {
                            Map<String, Object> stats = new java.util.HashMap<>();
                            stats.put("totalOrders", rs.getLong("totalOrders"));
                            stats.put("totalRevenue", rs.getBigDecimal("totalRevenue"));
                            stats.put("averageOrderValue", rs.getBigDecimal("averageOrderValue"));
                            stats.put("uniqueCustomers", rs.getLong("uniqueCustomers"));
                            return stats;
                        })
                .single();
    }

    /** Find orders with total amount greater than threshold */
    public List<Order> findOrdersAboveAmount(BigDecimal minAmount) {
        return jdbcClient
                .sql(
                        """
                SELECT id, customer_id, order_date, total_amount, status
                FROM orders
                WHERE total_amount > :minAmount
                ORDER BY total_amount DESC
                """)
                .param("minAmount", minAmount)
                .query(Order.class)
                .list();
    }

    /** Delete order by ID */
    public int deleteOrder(Long id) {
        return jdbcClient.sql("DELETE FROM orders WHERE id = :id").param("id", id).update();
    }

    /** Count orders by customer */
    public Long countOrdersByCustomer(Long customerId) {
        return jdbcClient
                .sql(
                        """
                SELECT COUNT(*)
                FROM orders
                WHERE customer_id = :customerId
                """)
                .param("customerId", customerId)
                .query(Long.class)
                .single();
    }
}
