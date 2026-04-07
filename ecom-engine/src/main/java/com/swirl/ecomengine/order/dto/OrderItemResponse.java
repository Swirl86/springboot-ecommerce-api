package com.swirl.ecomengine.order.dto;

public record OrderItemResponse(
        Long productId,
        String productName,
        double price,
        int quantity
) {}
