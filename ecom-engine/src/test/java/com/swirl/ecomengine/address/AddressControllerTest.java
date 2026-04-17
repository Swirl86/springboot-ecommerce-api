package com.swirl.ecomengine.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.address.controller.AddressController;
import com.swirl.ecomengine.address.dto.AddressResponse;
import com.swirl.ecomengine.address.dto.CreateOrUpdateAddressRequest;
import com.swirl.ecomengine.address.service.AddressService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;
import testsupport.TestDataFactory;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class AddressControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AddressService addressService;

    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    private User user;

    @BeforeEach
    void setup() throws Exception {
        user = TestDataFactory.user(pwd -> "encoded");

        when(authenticatedUserArgumentResolver.supportsParameter(any()))
                .thenReturn(true);

        when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(user);
    }

    // ---------------------------------------------------------
    // GET ADDRESS
    // ---------------------------------------------------------
    @Test
    void getAddress_shouldReturnAddress() throws Exception {
        AddressResponse response = TestDataFactory.addressResponse();

        when(addressService.get(user)).thenReturn(response);

        mockMvc.perform(get("/users/me/address"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Main Street 1"))
                .andExpect(jsonPath("$.city").value("Sundsvall"));
    }

    // ---------------------------------------------------------
    // CREATE OR UPDATE ADDRESS
    // ---------------------------------------------------------
    @Test
    void createOrUpdateAddress_shouldReturnUpdatedAddress() throws Exception {
        CreateOrUpdateAddressRequest req = TestDataFactory.addressRequest();
        AddressResponse response = TestDataFactory.addressResponse();

        when(addressService.createOrUpdate(user, req)).thenReturn(response);

        mockMvc.perform(post("/users/me/address")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.city").value("Sundsvall"));
    }
}