package com.swirl.ecomengine.product;

public record ProductUpdateRequest(
        String name,
        Double price,
        String description
) {}

