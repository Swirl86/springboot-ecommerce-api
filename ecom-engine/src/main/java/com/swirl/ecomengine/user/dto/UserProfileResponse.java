package com.swirl.ecomengine.user.dto;

public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String phone,
        int orderCount
) {}

