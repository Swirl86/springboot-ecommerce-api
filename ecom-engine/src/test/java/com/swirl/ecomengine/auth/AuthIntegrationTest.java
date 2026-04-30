package com.swirl.ecomengine.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.auth.dto.RefreshTokenRequest;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestSupportConfig;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityTestSupportConfig.class)
@ActiveProfiles("test-integration")
@Transactional
class AuthIntegrationTest {

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
    void register_shouldCreateUserInDatabase_andReturnTokens() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());

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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // login (success)
    // ============================================================

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
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
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // refresh token (success)
    // ============================================================

    @Test
    void refresh_shouldReturnNewTokens() throws Exception {
        // Register user
        RegisterRequest register = new RegisterRequest("refresh@example.com", "password123");

        String refreshToken = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String oldRefresh = objectMapper.readTree(refreshToken).get("refreshToken").asText();

        RefreshTokenRequest refreshRequest = new RefreshTokenRequest(oldRefresh);

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    // ============================================================
    // logout (success)
    // ============================================================

    @Test
    void logout_shouldReturnOk() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("dummy-refresh-token");

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}