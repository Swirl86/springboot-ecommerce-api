package com.swirl.ecomengine.product.dto;

public record ProductResponse(
        Long id,
        String name,
        double price,
        String description,
        Long categoryId,
        String categoryName
) {}
