package com.swirl.ecomengine.address.service;

import com.swirl.ecomengine.address.Address;
import com.swirl.ecomengine.address.AddressMapper;
import com.swirl.ecomengine.address.AddressRepository;
import com.swirl.ecomengine.address.dto.AddressResponse;
import com.swirl.ecomengine.address.dto.CreateOrUpdateAddressRequest;
import com.swirl.ecomengine.address.exception.AddressNotFoundException;
import com.swirl.ecomengine.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository repository;
    private final AddressMapper mapper;

    // ---------------------------------------------------------
    // CREATE OR UPDATE ADDRESS
    // ---------------------------------------------------------
    public AddressResponse createOrUpdate(User user, CreateOrUpdateAddressRequest req) {

        Address address = repository.findByUserId(user.getId());

        if (address == null) {
            address = new Address();
            address.setUser(user);
        }

        address.setStreet(req.street());
        address.setPostalCode(req.postalCode());
        address.setCity(req.city());
        address.setCountry(req.country());

        repository.save(address);

        return mapper.toResponse(address);
    }

    // ---------------------------------------------------------
    // GET ADDRESS
    // ---------------------------------------------------------
    public AddressResponse get(User user) {
        Address address = repository.findByUserId(user.getId());
        if (address == null) {
            throw new AddressNotFoundException(user.getId());
        }
        return mapper.toResponse(address);
    }

    // ---------------------------------------------------------
    // DELETE ADDRESS
    // ---------------------------------------------------------
    public void delete(User user) {
        Address address = repository.findByUserId(user.getId());
        if (address == null) {
            throw new AddressNotFoundException(user.getId());
        }
        repository.delete(address);
    }
}