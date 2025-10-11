package com.example.jdbcclient.controller;

import java.util.List;

import com.example.jdbcclient.dto.CustomerSummary;
import com.example.jdbcclient.model.Customer;
import com.example.jdbcclient.service.CustomerService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerService
                .getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Customer>> searchCustomers(
            @RequestParam String name, @RequestParam String status) {
        return ResponseEntity.ok(customerService.searchCustomers(name, status));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        return customerService
                .getCustomerByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Long> createCustomer(@RequestBody Customer customer) {
        Long customerId = customerService.createCustomer(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateCustomer(
            @PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        customerService.updateCustomer(customer);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch")
    public ResponseEntity<Void> createCustomersBatch(@RequestBody List<Customer> customers) {
        customerService.createCustomersBatch(customers);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/summaries")
    public ResponseEntity<List<CustomerSummary>> getCustomerSummaries() {
        return ResponseEntity.ok(customerService.getCustomerSummaries());
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveCustomers() {
        return ResponseEntity.ok(customerService.countActiveCustomers());
    }

    @GetMapping("/no-orders")
    public ResponseEntity<List<Customer>> getCustomersWithNoOrders() {
        return ResponseEntity.ok(customerService.getCustomersWithNoOrders());
    }

    @GetMapping("/top-active")
    public ResponseEntity<List<Customer>> getTopActiveCustomers(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(customerService.getTopActiveCustomers(limit));
    }
}
