package com.swirl.ecomengine.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.auth.controller.AuthController;
import com.swirl.ecomengine.auth.dto.AuthResponse;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.auth.exception.InvalidCredentialsException;
import com.swirl.ecomengine.common.exception.ConflictException;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    // ============================================================
    // register (success)
    // ============================================================

    @Test
    void register_shouldReturnAuthResponse() throws Exception {
        RegisterRequest request = TestDataFactory.registerRequest();
        AuthResponse response = TestDataFactory.authResponse();

        when(authService.register(request)).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // ============================================================
    // register (duplicate email → 409)
    // ============================================================

    @Test
    void register_shouldReturnConflict_whenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

        when(authService.register(request))
                .thenThrow(new ConflictException("Email already in use"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    // ============================================================
    // register (empty email → validation → 400)
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
    // register (empty password → validation → 400)
    // ============================================================

    @Test
    void register_shouldReturnBadRequest_whenPasswordIsEmpty() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "");

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
    void login_shouldReturnAuthResponse() throws Exception {
        LoginRequest request = TestDataFactory.loginRequest();
        AuthResponse response = TestDataFactory.authResponse();

        when(authService.login(request)).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // ============================================================
    // login (invalid credentials → 401)
    // ============================================================

    @Test
    void login_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpass");

        when(authService.login(request))
                .thenThrow(new InvalidCredentialsException());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}