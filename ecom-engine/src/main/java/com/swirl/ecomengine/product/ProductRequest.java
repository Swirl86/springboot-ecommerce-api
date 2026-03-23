package com.swirl.ecomengine.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ProductRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Positive(message = "Price must be greater than 0")
        double price,

        String description,
        Long categoryId
) {}

