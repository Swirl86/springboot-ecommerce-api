package com.swirl.ecomengine.auth;

import com.swirl.ecomengine.auth.dto.AuthResponse;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.auth.exception.InvalidCredentialsException;
import com.swirl.ecomengine.auth.repository.RefreshTokenRepository;
import com.swirl.ecomengine.common.exception.EmailAlreadyExistsException;
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
    private RefreshTokenRepository refreshTokenRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setup() {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);

        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                jwtService
        );
    }

    // ============================================================
    // register (success)
    // ============================================================

    @Test
    void register_shouldSaveUserAndReturnTokens() {
        RegisterRequest request = TestDataFactory.registerRequest();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(jwtService.generateToken(any(User.class))).thenReturn("access-token");

        // Mock refresh token creation
        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken rt = invocation.getArgument(0);
                    rt.setId(1L);
                    return rt;
                });

        AuthResponse response = authService.register(request);

        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotNull();

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
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    // ============================================================
    // login (valid credentials)
    // ============================================================

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        LoginRequest request = TestDataFactory.loginRequest();

        User user = User.builder()
                .id(1L)
                .email(request.email())
                .password("hashed")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("access-token");

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> {
                    RefreshToken rt = invocation.getArgument(0);
                    rt.setId(1L);
                    return rt;
                });

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotNull();
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