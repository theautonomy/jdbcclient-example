package com.example.jdbcclient.service;

import java.util.List;
import java.util.Optional;

import com.example.jdbcclient.dto.CustomerSummary;
import com.example.jdbcclient.model.Customer;
import com.example.jdbcclient.repository.CustomerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public List<Customer> searchCustomers(String name, String status) {
        return customerRepository.findByNameAndStatus(name, status);
    }

    public Long createCustomer(Customer customer) {
        return customerRepository.createCustomer(customer);
    }

    public void updateCustomer(Customer customer) {
        customerRepository.updateCustomer(customer);
    }

    public void deleteCustomer(Long id) {
        customerRepository.deleteCustomer(id);
    }

    public void createCustomersBatch(List<Customer> customers) {
        customerRepository.createCustomersBatch(customers);
    }

    public List<CustomerSummary> getCustomerSummaries() {
        return customerRepository.findCustomerSummaries();
    }

    public Long countActiveCustomers() {
        return customerRepository.countByStatus("ACTIVE");
    }

    public List<Customer> getCustomersWithNoOrders() {
        return customerRepository.findCustomersWithNoOrders();
    }

    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public List<Customer> getTopActiveCustomers(int limit) {
        return customerRepository.findTopActiveCustomers(limit);
    }
}
