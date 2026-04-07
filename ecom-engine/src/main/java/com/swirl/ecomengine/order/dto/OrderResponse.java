package com.swirl.ecomengine.order.dto;

import com.swirl.ecomengine.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        double totalPrice,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}
