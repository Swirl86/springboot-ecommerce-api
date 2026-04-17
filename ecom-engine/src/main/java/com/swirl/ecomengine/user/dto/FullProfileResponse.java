package com.swirl.ecomengine.user.dto;

import com.swirl.ecomengine.address.dto.AddressResponse;

public record FullProfileResponse(
        Long id,
        String email,
        String name,
        String phone,
        int orderCount,
        AddressResponse address
) {}