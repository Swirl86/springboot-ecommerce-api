package com.swirl.ecomengine.address;

import com.swirl.ecomengine.address.dto.AddressResponse;
import com.swirl.ecomengine.address.dto.CreateOrUpdateAddressRequest;
import com.swirl.ecomengine.address.exception.AddressNotFoundException;
import com.swirl.ecomengine.address.service.AddressService;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceTest {

    private AddressRepository repository;
    private AddressMapper mapper;
    private AddressService service;

    @BeforeEach
    void setup() {
        repository = mock(AddressRepository.class);
        mapper = mock(AddressMapper.class);
        service = new AddressService(repository, mapper);
    }

    // ---------------------------------------------------------
    // CREATE OR UPDATE
    // ---------------------------------------------------------
    @Test
    void createOrUpdate_shouldCreateNewAddress_whenNoneExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        CreateOrUpdateAddressRequest req = TestDataFactory.addressRequest();
        AddressResponse response = TestDataFactory.addressResponse();

        when(repository.findByUserId(1L)).thenReturn(null);
        when(mapper.toResponse(any(Address.class))).thenReturn(response);

        AddressResponse result = service.createOrUpdate(user, req);

        assertThat(result.street()).isEqualTo("Main Street 1");
        assertThat(result.city()).isEqualTo("Sundsvall");

        verify(repository).save(any(Address.class));
        verify(mapper).toResponse(any(Address.class));
    }

    @Test
    void createOrUpdate_shouldUpdateExistingAddress() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Address existing = new Address();
        existing.setStreet("Old Street");
        existing.setUser(user);

        CreateOrUpdateAddressRequest req = TestDataFactory.updatedAddressRequest();
        AddressResponse response = TestDataFactory.updatedAddressResponse();


        when(repository.findByUserId(1L)).thenReturn(existing);
        when(mapper.toResponse(existing)).thenReturn(response);

        AddressResponse result = service.createOrUpdate(user, req);

        assertThat(result.street()).isEqualTo("Updated Street");
        assertThat(existing.getCity()).isEqualTo("Stockholm");

        verify(repository).save(existing);
        verify(mapper).toResponse(existing);
    }

    // ---------------------------------------------------------
    // GET
    // ---------------------------------------------------------
    @Test
    void get_shouldThrow_whenNoAddressExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        when(repository.findByUserId(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.get(user))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void get_shouldReturnAddressResponse_whenExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Address address = new Address();
        address.setStreet("Main Street 1");

        AddressResponse response = TestDataFactory.addressResponse();

        when(repository.findByUserId(1L)).thenReturn(address);
        when(mapper.toResponse(address)).thenReturn(response);

        AddressResponse result = service.get(user);

        assertThat(result.street()).isEqualTo("Main Street 1");
        verify(mapper).toResponse(address);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    @Test
    void delete_shouldThrow_whenNoAddressExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        when(repository.findByUserId(1L)).thenReturn(null);

        assertThatThrownBy(() -> service.delete(user))
                .isInstanceOf(AddressNotFoundException.class);
    }

    @Test
    void delete_shouldDeleteAddress_whenExists() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);

        Address address = new Address();
        when(repository.findByUserId(1L)).thenReturn(address);

        service.delete(user);

        verify(repository).delete(address);
    }
}