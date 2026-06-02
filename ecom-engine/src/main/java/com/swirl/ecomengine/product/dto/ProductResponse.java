package com.swirl.ecomengine.product.dto;

import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        double price,
        String description,
        Long categoryId,
        String categoryName,
        List<String> imageUrls,
        List<ProductTagResponse> tags
) {}
