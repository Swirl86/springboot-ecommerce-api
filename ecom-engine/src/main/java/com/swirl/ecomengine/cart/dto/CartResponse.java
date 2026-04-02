package com.swirl.ecomengine.cart.dto;

import java.util.List;

public record CartResponse(
        Long id,
        Long userId,
        List<CartItemResponse> items,
        double total
) {}

