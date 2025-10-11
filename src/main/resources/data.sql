-- Insert sample customers
INSERT INTO customers (name, email, status) VALUES
('John Doe', 'john.doe@example.com', 'ACTIVE'),
('Jane Smith', 'jane.smith@example.com', 'ACTIVE'),
('Bob Johnson', 'bob.johnson@example.com', 'INACTIVE'),
('Alice Williams', 'alice.williams@example.com', 'ACTIVE'),
('Charlie Brown', 'charlie.brown@example.com', 'ACTIVE');

-- Insert sample products
INSERT INTO products (name, price, stock_quantity) VALUES
('Laptop', 999.99, 50),
('Mouse', 29.99, 200),
('Keyboard', 79.99, 150),
('Monitor', 299.99, 75),
('USB Cable', 9.99, 500);

-- Insert sample orders
INSERT INTO orders (customer_id, order_date, total_amount, status) VALUES
(1, CURRENT_TIMESTAMP - INTERVAL '5' DAY, 1109.97, 'COMPLETED'),
(2, CURRENT_TIMESTAMP - INTERVAL '3' DAY, 389.97, 'COMPLETED'),
(1, CURRENT_TIMESTAMP - INTERVAL '2' DAY, 79.99, 'SHIPPED'),
(4, CURRENT_TIMESTAMP - INTERVAL '1' DAY, 299.99, 'PENDING'),
(5, CURRENT_TIMESTAMP, 39.98, 'PENDING');

-- Insert sample order items
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
-- Order 1
(1, 1, 1, 999.99),
(1, 2, 1, 29.99),
(1, 5, 8, 9.99),
-- Order 2
(2, 4, 1, 299.99),
(2, 3, 1, 79.99),
(2, 5, 1, 9.99),
-- Order 3
(3, 3, 1, 79.99),
-- Order 4
(4, 4, 1, 299.99),
-- Order 5
(5, 2, 1, 29.99),
(5, 5, 1, 9.99);
