package com.swirl.ecomengine.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.auth.controller.AuthController;
import com.swirl.ecomengine.auth.dto.AuthResponse;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
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
    // register
    // ============================================================

    @Test
    void register_shouldReturnAuthResponse() throws Exception {
        // ---------- Arrange ----------
        RegisterRequest request = TestDataFactory.registerRequest();
        AuthResponse response = TestDataFactory.authResponse();

        when(authService.register(request)).thenReturn(response);

        // ---------- Act & Assert ----------
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    // ============================================================
    // login
    // ============================================================

    @Test
    void login_shouldReturnAuthResponse() throws Exception {
        // ---------- Arrange ----------
        LoginRequest request = TestDataFactory.loginRequest();
        AuthResponse response = TestDataFactory.authResponse();

        when(authService.login(request)).thenReturn(response);

        // ---------- Act & Assert ----------
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }
}