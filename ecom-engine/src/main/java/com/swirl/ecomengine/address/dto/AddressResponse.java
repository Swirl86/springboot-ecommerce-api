package com.swirl.ecomengine.address.dto;

public record AddressResponse(
        Long id,
        String street,
        String postalCode,
        String city,
        String country
) {}