package com.swirl.ecomengine.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.address.AddressRepository;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.dto.UpdateUserProfileRequest;
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

class UserIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtService jwtService;

    private String jwtToken;
    private User user;

    @BeforeEach
    void setup() {
        addressRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(TestDataFactory.user(passwordEncoder));

        jwtToken = jwtService.generateToken(user);
    }

    // ---------------------------------------------------------
    // GET FULL PROFILE
    // ---------------------------------------------------------
    @Test
    void getFullProfile_shouldReturnUserAndAddress() throws Exception {
        mockMvc.perform(get("/users/me/full-profile")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.address").doesNotExist());
    }

    // ---------------------------------------------------------
    // UPDATE PROFILE
    // ---------------------------------------------------------
    @Test
    void updateProfile_shouldUpdateNameEmailPhone() throws Exception {

        UpdateUserProfileRequest req = new UpdateUserProfileRequest(
                "New Name",
                "new@example.com",
                "0709999999",
                null,
                null
        );

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Name"))
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.phone").value("0709999999"));

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getPhone()).isEqualTo("0709999999");
    }

    // ---------------------------------------------------------
    // UPDATE PASSWORD
    // ---------------------------------------------------------
    @Test
    void updateProfile_shouldUpdatePassword_whenCurrentPasswordMatches() throws Exception {

        UpdateUserProfileRequest req = new UpdateUserProfileRequest(
                null,
                null,
                null,
                "password123",   // current
                "newPassword!"   // new
        );

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newPassword!", updated.getPassword())).isTrue();
    }

    // ---------------------------------------------------------
    // CREATE OR UPDATE ADDRESS
    // ---------------------------------------------------------
    @Test
    void createOrUpdateAddress_shouldPersistAddress() throws Exception {

        var req = TestDataFactory.addressRequest();

        mockMvc.perform(post("/users/me/address")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.street").value("Main Street 1"));

        var address = addressRepository.findByUserId(user.getId());
        assertThat(address).isNotNull();
        assertThat(address.getCity()).isEqualTo("Sundsvall");
    }
}