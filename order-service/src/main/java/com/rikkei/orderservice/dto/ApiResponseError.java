package com.rikkei.orderservice.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponseError {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
