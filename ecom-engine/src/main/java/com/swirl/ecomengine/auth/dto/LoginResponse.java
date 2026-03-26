package com.swirl.ecomengine.auth.dto;

public record LoginResponse(
        String token,
        Long userId,
        String email,
        String role
) {}
