package com.swirl.ecomengine.address.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateOrUpdateAddressRequest(
        @NotBlank String street,
        @NotBlank String postalCode,
        @NotBlank String city,
        @NotBlank String country
) {}