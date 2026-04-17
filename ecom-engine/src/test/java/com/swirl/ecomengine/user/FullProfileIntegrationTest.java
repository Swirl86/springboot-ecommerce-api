package com.swirl.ecomengine.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.address.AddressRepository;
import com.swirl.ecomengine.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FullProfileIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
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

    @Test
    void fullProfile_shouldReturnUserAndAddress() throws Exception {
        var address = addressRepository.save(TestDataFactory.address(user));

        mockMvc.perform(get("/users/me/full-profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.address.street").value("Main Street 1"))
                .andExpect(jsonPath("$.address.city").value("Sundsvall"));
    }
}