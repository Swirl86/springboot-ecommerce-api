package com.swirl.ecomengine.user.dto;

import com.swirl.ecomengine.user.Role;

public record UserResponse(
        Long id,
        String email,
        Role role
) {}
