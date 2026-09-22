package com.rikkei.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {
    private Long customerId;
    private Long productId;
    private Integer quantity;
}
