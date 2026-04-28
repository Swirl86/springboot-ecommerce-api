package com.swirl.ecomengine.category.dto;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        LocalDateTime updatedAt
) {}

