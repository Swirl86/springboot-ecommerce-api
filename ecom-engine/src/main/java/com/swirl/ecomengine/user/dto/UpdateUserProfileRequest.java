package com.swirl.ecomengine.user.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
        @Size(min = 2, max = 50, message = "Name must be between 2–50 characters")
        String name,

        @Email(message = "Invalid email format")
        String email,

        @Pattern(
                regexp = "^[0-9+\\- ]{6,20}$",
                message = "Invalid phone number format"
        )
        String phone,

        String currentPassword,
        String newPassword
) {}

