# Spring JdbcClient Example

This is a comprehensive Spring Boot application demonstrating the usage of Spring's modern **JdbcClient** API (introduced in Spring Framework 6.1) with an H2 in-memory database.

This project accompanies the guide at: https://theautonomy.github.io/articles/spring-jdbcclient-guide.html

## Features

### JdbcClient Examples Demonstrated

The application showcases various JdbcClient patterns including:

1. **Simple Queries** - Basic SELECT queries
2. **Parameterized Queries** - Using named parameters (`:paramName`)
3. **Insert Operations** - Creating new records with key generation
4. **Update Operations** - Modifying existing records
5. **Delete Operations** - Removing records
6. **Batch Operations** - Processing multiple records
7. **Complex Queries** - JOIN operations and aggregations
8. **Single Value Queries** - Extracting scalar values
9. **Custom Row Mapping** - Using lambda expressions for result mapping
10. **Optional Results** - Handling queries that may return no results

## Project Structure

```
src/main/java/com/example/jdbcclient/
├── model/
│   ├── Customer.java
│   ├── Order.java
│   ├── OrderItem.java
│   └── Product.java
├── dto/
│   └── CustomerSummary.java
├── repository/
│   ├── CustomerRepository.java
│   └── OrderRepository.java
├── service/
│   ├── CustomerService.java
│   └── OrderService.java
├── controller/
│   ├── CustomerController.java
│   └── OrderController.java
└── JdbcClientExampleApplication.java

src/main/resources/
├── schema.sql
├── data.sql
└── application.properties
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## H2 Console

Access the H2 database console at: `http://localhost:8080/h2-console`

**Connection details:**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (leave blank)

## API Endpoints

### Customer Endpoints

#### Get All Customers
```bash
GET http://localhost:8080/api/customers
```

#### Get Customer by ID
```bash
GET http://localhost:8080/api/customers/{id}
```

#### Search Customers
```bash
GET http://localhost:8080/api/customers/search?name=John&status=ACTIVE
```

#### Get Customer by Email
```bash
GET http://localhost:8080/api/customers/email/{email}
```

#### Create Customer
```bash
POST http://localhost:8080/api/customers
Content-Type: application/json

{
  "name": "Test User",
  "email": "test@example.com",
  "status": "ACTIVE"
}
```

#### Update Customer
```bash
PUT http://localhost:8080/api/customers/{id}
Content-Type: application/json

{
  "name": "Updated Name",
  "email": "updated@example.com",
  "status": "ACTIVE"
}
```

#### Delete Customer
```bash
DELETE http://localhost:8080/api/customers/{id}
```

#### Batch Create Customers
```bash
POST http://localhost:8080/api/customers/batch
Content-Type: application/json

[
  {
    "name": "User 1",
    "email": "user1@example.com",
    "status": "ACTIVE"
  },
  {
    "name": "User 2",
    "email": "user2@example.com",
    "status": "ACTIVE"
  }
]
```

#### Get Customer Summaries (with order statistics)
```bash
GET http://localhost:8080/api/customers/summaries
```

#### Count Active Customers
```bash
GET http://localhost:8080/api/customers/count/active
```

#### Get Customers with No Orders
```bash
GET http://localhost:8080/api/customers/no-orders
```

#### Get Top Active Customers
```bash
GET http://localhost:8080/api/customers/top-active?limit=5
```

### Order Endpoints

#### Get All Orders
```bash
GET http://localhost:8080/api/orders
```

#### Get Order by ID
```bash
GET http://localhost:8080/api/orders/{id}
```

#### Get Orders by Customer ID
```bash
GET http://localhost:8080/api/orders/customer/{customerId}
```

#### Get Orders by Status
```bash
GET http://localhost:8080/api/orders/status/{status}
```

#### Create Order
```bash
POST http://localhost:8080/api/orders
Content-Type: application/json

{
  "customerId": 1,
  "totalAmount": 299.99,
  "status": "PENDING"
}
```

#### Update Order Status
```bash
PATCH http://localhost:8080/api/orders/{id}/status?status=COMPLETED
```

#### Delete Order
```bash
DELETE http://localhost:8080/api/orders/{id}
```

#### Get Total Revenue
```bash
GET http://localhost:8080/api/orders/revenue/total
```

#### Get Order Statistics
```bash
GET http://localhost:8080/api/orders/statistics
```

#### Get Orders Above Amount
```bash
GET http://localhost:8080/api/orders/above-amount?minAmount=100
```

#### Count Orders by Customer
```bash
GET http://localhost:8080/api/orders/count/customer/{customerId}
```

## Key Code Examples

### Simple Query with JdbcClient

```java
public List<Customer> findAll() {
    return jdbcClient.sql("SELECT id, name, email, status, created_at FROM customers")
            .query(Customer.class)
            .list();
}
```

### Parameterized Query

```java
public Optional<Customer> findById(Long id) {
    return jdbcClient.sql("SELECT id, name, email, status, created_at FROM customers WHERE id = :id")
            .param("id", id)
            .query(Customer.class)
            .optional();
}
```

### Insert with Key Generation

```java
public Long createCustomer(Customer customer) {
    KeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcClient.sql("""
            INSERT INTO customers (name, email, status)
            VALUES (:name, :email, :status)
            """)
            .param("name", customer.getName())
            .param("email", customer.getEmail())
            .param("status", customer.getStatus())
            .update(keyHolder);

    return keyHolder.getKey().longValue();
}
```

### Complex Query with JOIN

```java
public List<CustomerSummary> findCustomerSummaries() {
    return jdbcClient.sql("""
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
```

### Custom Row Mapper

```java
public Map<String, Object> getOrderStatistics() {
    return jdbcClient.sql("""
            SELECT
                COUNT(*) as totalOrders,
                COALESCE(SUM(total_amount), 0) as totalRevenue,
                COALESCE(AVG(total_amount), 0) as averageOrderValue,
                COUNT(DISTINCT customer_id) as uniqueCustomers
            FROM orders
            """)
            .query((rs, rowNum) -> Map.of(
                "totalOrders", rs.getLong("totalOrders"),
                "totalRevenue", rs.getBigDecimal("totalRevenue"),
                "averageOrderValue", rs.getBigDecimal("averageOrderValue"),
                "uniqueCustomers", rs.getLong("uniqueCustomers")
            ))
            .single();
}
```

## Technologies Used

- Spring Boot 3.2.5
- Spring JDBC with JdbcClient
- H2 Database (in-memory)
- Java 17
- Lombok
- Maven

## Database Schema

The application uses four main tables:
- **customers** - Customer information
- **products** - Product catalog
- **orders** - Customer orders
- **order_items** - Order line items

See `schema.sql` for the complete schema definition.

## Sample Data

The application comes pre-loaded with sample data (see `data.sql`):
- 5 customers
- 5 products
- 5 orders
- 10 order items

## Benefits of JdbcClient

1. **Fluent API** - Chainable method calls for better readability
2. **Named Parameters** - More maintainable than positional parameters
3. **Type Safety** - Compile-time checking with generics
4. **Modern Java** - Supports Records, text blocks, and lambda expressions
5. **Simplified Mapping** - Automatic mapping to POJOs and Records
6. **Less Boilerplate** - Compared to traditional JdbcTemplate

## License

MIT
