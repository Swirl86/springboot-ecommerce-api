package com.swirl.ecomengine.product;

public record ProductResponse(
        Long id,
        String name,
        double price,
        String description
) {}
