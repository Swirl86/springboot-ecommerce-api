package com.swirl.ecomengine.product.dto.review;

import java.time.LocalDateTime;

public record ProductReviewResponse(
        Long id,
        int rating,
        String comment,
        String username,
        LocalDateTime createdAt,
        LocalDateTime lastEditedAt
) {}

