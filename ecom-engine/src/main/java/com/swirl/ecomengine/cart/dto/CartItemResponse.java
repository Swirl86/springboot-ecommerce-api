package com.swirl.ecomengine.cart.dto;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        double unitPrice,
        int quantity,
        double totalPrice
) {}
