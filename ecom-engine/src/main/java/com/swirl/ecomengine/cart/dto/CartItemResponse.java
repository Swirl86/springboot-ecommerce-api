package com.swirl.ecomengine.cart.dto;

public record CartItemResponse(
        Long id,
        Long productId,
        String productName,
        String imageUrl,
        double unitPrice,
        int quantity,
        double totalPrice
) {}
