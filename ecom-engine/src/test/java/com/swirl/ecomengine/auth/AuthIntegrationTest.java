package com.swirl.ecomengine.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.IntegrationTestBase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    // ============================================================
    // register (success)
    // ============================================================

    @Test
    void register_shouldCreateUserInDatabase_andReturnJwt() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").exists());

        assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
    }

    // ============================================================
    // register (validation: empty email → 400)
    // ============================================================

    @Test
    void register_shouldReturnBadRequest_whenEmailIsEmpty() throws Exception {
        RegisterRequest request = new RegisterRequest("", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("{email=Email must be between 2 and 50 characters}"));
    }

    // ============================================================
    // register (validation: empty password → 400)
    // ============================================================

    @Test
    void register_shouldReturnBadRequest_whenPasswordIsEmpty() throws Exception {
        RegisterRequest request = new RegisterRequest("valid@example.com", "");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("{password=Password must be at least 8 characters}"));
    }

    // ============================================================
    // login (success)
    // ============================================================

    @Test
    void login_shouldReturnJwt_whenCredentialsAreValid() throws Exception {
        RegisterRequest register = new RegisterRequest("login@example.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest("login@example.com", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("login@example.com"));
    }

    // ============================================================
    // login (invalid password → 401)
    // ============================================================

    @Test
    void login_shouldReturnUnauthorized_whenPasswordIsWrong() throws Exception {
        RegisterRequest register = new RegisterRequest("wrongpass@example.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest("wrongpass@example.com", "incorrectpass");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    // ============================================================
    // login (validation: empty email → 400)
    // ============================================================

    @Test
    void login_shouldReturnBadRequest_whenEmailIsEmpty() throws Exception {
        LoginRequest login = new LoginRequest("", "password123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("{email=Email is required}"));
    }

    // ============================================================
    // login (validation: empty password → 400)
    // ============================================================

    @Test
    void login_shouldReturnBadRequest_whenPasswordIsEmpty() throws Exception {
        LoginRequest login = new LoginRequest("valid@example.com", "");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("{password=Password is required}"));
    }
}