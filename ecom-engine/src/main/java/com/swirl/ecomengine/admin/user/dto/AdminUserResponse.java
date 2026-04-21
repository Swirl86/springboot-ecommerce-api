package com.swirl.ecomengine.admin.user.dto;

import com.swirl.ecomengine.user.Role;

public record AdminUserResponse(
        Long id,
        String email,
        String name,
        String phone,
        Role role,
        int orderCount
) {}