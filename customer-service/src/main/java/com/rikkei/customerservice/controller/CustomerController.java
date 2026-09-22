package com.rikkei.customerservice.controller;

import com.rikkei.customerservice.model.Customer;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final Map<Long, Customer> customerRepository = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void initData() {
        Customer c1 = Customer.builder()
                .id(idCounter.getAndIncrement())
                .fullName("Nguyễn Văn An")
                .email("an.nguyen@example.com")
                .phone("0901234567")
                .address("Hà Nội")
                .build();
        Customer c2 = Customer.builder()
                .id(idCounter.getAndIncrement())
                .fullName("Trần Thị Bình")
                .email("binh.tran@example.com")
                .phone("0987654321")
                .address("TP. Hồ Chí Minh")
                .build();
        customerRepository.put(c1.getId(), c1);
        customerRepository.put(c2.getId(), c2);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(new ArrayList<>(customerRepository.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerRepository.get(id);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Long newId = idCounter.getAndIncrement();
        customer.setId(newId);
        customerRepository.put(newId, customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(customer);
    }
}
