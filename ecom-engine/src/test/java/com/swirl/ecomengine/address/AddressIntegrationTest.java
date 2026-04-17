package com.swirl.ecomengine.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AddressIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;

    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private User user;
    private String token;

    @BeforeEach
    void setup() {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(TestDataFactory.user(passwordEncoder));
        token = jwtService.generateToken(user);
    }

    // ---------------------------------------------------------
    // GET ADDRESS
    // ---------------------------------------------------------
    @Test
    void getAddress_shouldReturn404_whenNoAddressExists() throws Exception {
        mockMvc.perform(get("/users/me/address")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAddress_shouldReturnAddress_whenExists() throws Exception {
        var req = TestDataFactory.addressRequest();

        mockMvc.perform(post("/users/me/address")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/me/address")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Main Street 1"))
                .andExpect(jsonPath("$.city").value("Sundsvall"));
    }

    // ---------------------------------------------------------
    // CREATE OR UPDATE ADDRESS
    // ---------------------------------------------------------
    @Test
    void createOrUpdateAddress_shouldCreateNewAddress() throws Exception {
        var req = TestDataFactory.addressRequest();

        mockMvc.perform(post("/users/me/address")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Main Street 1"));

        var address = addressRepository.findByUserId(user.getId());
        assertThat(address).isNotNull();
        assertThat(address.getPostalCode()).isEqualTo("12345");
    }

    @Test
    void createOrUpdateAddress_shouldUpdateExistingAddress() throws Exception {
        mockMvc.perform(post("/users/me/address")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.addressRequest())))
                .andExpect(status().isOk());

        var updateReq = TestDataFactory.updatedAddressRequest();

        mockMvc.perform(post("/users/me/address")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Updated Street"))
                .andExpect(jsonPath("$.city").value("Stockholm"));

        var updated = addressRepository.findByUserId(user.getId());
        assertThat(updated.getStreet()).isEqualTo("Updated Street");
        assertThat(updated.getCity()).isEqualTo("Stockholm");
    }

    // ---------------------------------------------------------
    // DELETE ADDRESS
    // ---------------------------------------------------------
    @Test
    void deleteAddress_shouldRemoveAddress() throws Exception {
        // Create first
        mockMvc.perform(post("/users/me/address")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.addressRequest())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/users/me/address")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(addressRepository.findByUserId(user.getId())).isNull();
    }
}