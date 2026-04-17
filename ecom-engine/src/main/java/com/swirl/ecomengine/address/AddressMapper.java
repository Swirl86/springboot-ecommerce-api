package com.swirl.ecomengine.address;

import com.swirl.ecomengine.address.dto.AddressResponse;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressResponse toResponse(Address a) {
        return new AddressResponse(
                a.getId(),
                a.getStreet(),
                a.getPostalCode(),
                a.getCity(),
                a.getCountry()
        );
    }
}
