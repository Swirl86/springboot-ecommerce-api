package com.swirl.ecomengine.wishlist.dto;

public record WishlistResponse(
        Long id,
        Long productId,
        String name,
        double price,
        String description,
        Long categoryId
) {}