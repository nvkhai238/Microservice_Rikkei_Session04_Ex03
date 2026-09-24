package com.rikkei.orderservice.controller;

import com.rikkei.orderservice.dto.OrderRequestDTO;
import com.rikkei.orderservice.dto.ProductResponseDTO;
import com.rikkei.orderservice.exception.ServiceUnavailableException;
import com.rikkei.orderservice.model.Order;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    private final Map<Long, Order> orderRepository = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    @PostConstruct
    public void initData() {
        Order o1 = Order.builder()
                .id(idCounter.getAndIncrement())
                .customerId(1L)
                .productId(1L)
                .quantity(1)
                .totalPrice(new BigDecimal("35000000"))
                .status("COMPLETED")
                .createdAt(LocalDateTime.now())
                .build();
        orderRepository.put(o1.getId(), o1);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(new ArrayList<>(orderRepository.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderRepository.get(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/discovery/product-service")
    public ResponseEntity<Map<String, Object>> discoverProductService() {
        List<ServiceInstance> instances = discoveryClient.getInstances("PRODUCT-SERVICE");
        if (instances == null || instances.isEmpty()) {
            throw new ServiceUnavailableException("503 Service Unavailable: Không tìm thấy instance nào của PRODUCT-SERVICE trên Eureka Server");
        }

        ServiceInstance instance = instances.get(0);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("serviceId", instance.getServiceId());
        result.put("host", instance.getHost());
        result.put("port", instance.getPort());
        result.put("uri", instance.getUri().toString());
        result.put("instanceCount", instances.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/create-with-discovery")
    public ResponseEntity<Order> createOrderWithDiscovery(@RequestBody OrderRequestDTO request) {
        log.info("Bắt đầu quy trình tạo đơn hàng cho Customer ID: {}, Product ID: {}", request.getCustomerId(), request.getProductId());

        List<ServiceInstance> instances = discoveryClient.getInstances("PRODUCT-SERVICE");
        if (instances == null || instances.isEmpty()) {
            log.error("Không tìm thấy instance nào của PRODUCT-SERVICE trên Eureka Server!");
            throw new ServiceUnavailableException("503 Service Unavailable: Không tìm thấy instance nào của PRODUCT-SERVICE trên Eureka Server");
        }

        ServiceInstance productInstance = instances.get(0);
        String baseUrl = productInstance.getUri().toString();
        String productUrl = baseUrl + "/api/v1/products/" + request.getProductId();
        log.info("Tìm thấy PRODUCT-SERVICE tại URL: {}. Tiến hành gọi lấy thông tin sản phẩm...", productUrl);

        ProductResponseDTO product;
        try {
            product = restTemplate.getForObject(productUrl, ProductResponseDTO.class);
        } catch (RestClientException ex) {
            log.error("Lỗi khi kết nối tới PRODUCT-SERVICE tại {}: {}", productUrl, ex.getMessage());
            throw new ServiceUnavailableException("503 Service Unavailable: Lỗi kết nối tới PRODUCT-SERVICE: " + ex.getMessage());
        }

        if (product == null) {
            throw new ServiceUnavailableException("503 Service Unavailable: Không thể lấy thông tin sản phẩm từ PRODUCT-SERVICE");
        }

        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        Long newId = idCounter.getAndIncrement();
        Order order = Order.builder()
                .id(newId)
                .customerId(request.getCustomerId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .status("CREATED")
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.put(newId, order);
        log.info("Tạo đơn hàng thành công với ID: {}, Tổng tiền: {}", newId, totalPrice);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }
}
