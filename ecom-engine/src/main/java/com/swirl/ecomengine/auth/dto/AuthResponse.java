package com.swirl.ecomengine.auth.dto;

public record AuthResponse(
        Long userId,
        String email,
        String role,
        String token
) {}
