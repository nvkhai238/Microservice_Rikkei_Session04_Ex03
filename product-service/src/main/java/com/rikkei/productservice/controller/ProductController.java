package com.rikkei.productservice.controller;

import com.rikkei.productservice.model.Product;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Value("${server.port}")
    private String serverPort;

    private final Map<Long, Product> productRepository = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void initData() {
        Product p1 = Product.builder()
                .id(idCounter.getAndIncrement())
                .name("Laptop Dell XPS 15")
                .price(new BigDecimal("35000000"))
                .stockQuantity(15)
                .description("Laptop doanh nhân cao cấp màn hình OLED")
                .build();
        Product p2 = Product.builder()
                .id(idCounter.getAndIncrement())
                .name("iPhone 16 Pro Max")
                .price(new BigDecimal("32000000"))
                .stockQuantity(30)
                .description("Điện thoại Apple cao cấp nhất")
                .build();
        Product p3 = Product.builder()
                .id(idCounter.getAndIncrement())
                .name("Bàn phím cơ Keychron Q1")
                .price(new BigDecimal("4200000"))
                .stockQuantity(50)
                .description("Bàn phím cơ custom nhôm nguyên khối")
                .build();
        productRepository.put(p1.getId(), p1);
        productRepository.put(p2.getId(), p2);
        productRepository.put(p3.getId(), p3);
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(new ArrayList<>(productRepository.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productRepository.get(id);
        if (product != null) {
            return ResponseEntity.ok(product);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Long newId = idCounter.getAndIncrement();
        product.setId(newId);
        productRepository.put(newId, product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }
}
