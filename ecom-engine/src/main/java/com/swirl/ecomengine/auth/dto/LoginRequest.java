package com.swirl.ecomengine.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}
