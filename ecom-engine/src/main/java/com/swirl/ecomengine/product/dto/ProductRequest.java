package com.swirl.ecomengine.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ProductRequest(
        @NotBlank(message = "Name is required")
        String name,

        @Positive(message = "Price must be greater than 0")
        double price,

        String description,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        List<String> imageUrls
) {}

