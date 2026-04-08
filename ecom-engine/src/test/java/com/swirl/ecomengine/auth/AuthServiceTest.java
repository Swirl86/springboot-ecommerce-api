package com.swirl.ecomengine.auth;

import com.swirl.ecomengine.auth.dto.AuthResponse;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.auth.exception.InvalidCredentialsException;
import com.swirl.ecomengine.common.exception.ConflictException;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.Role;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import testsupport.TestDataFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    // ============================================================
    // register (success)
    // ============================================================

    @Test
    void register_shouldSaveUserAndReturnToken() {
        RegisterRequest request = TestDataFactory.registerRequest();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.token()).isEqualTo("jwt-token");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
    }

    // ============================================================
    // register (duplicate email)
    // ============================================================

    @Test
    void register_shouldThrowConflict_whenEmailExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email already in use");
    }

    // ============================================================
    // login (valid credentials)
    // ============================================================

    @Test
    void login_shouldReturnToken_whenCredentialsAreValid() {
        LoginRequest request = TestDataFactory.loginRequest();

        User user = User.builder()
                .id(1L)
                .email(request.email())
                .password("hashed")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo("test@example.com");
    }

    // ============================================================
    // login (invalid password)
    // ============================================================

    @Test
    void login_shouldThrowException_whenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong");

        User user = User.builder()
                .email("test@example.com")
                .password("hashed")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}