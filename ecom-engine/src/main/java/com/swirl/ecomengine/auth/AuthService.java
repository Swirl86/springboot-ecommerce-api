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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(14);

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // -------------------------
    // REGISTER
    // -------------------------
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                refreshToken
        );
    }

    // -------------------------
    // LOGIN
    // -------------------------
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = createRefreshToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                accessToken,
                refreshToken
        );
    }

    // -------------------------
    // REFRESH TOKEN
    // -------------------------
    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(InvalidCredentialsException::new);

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException();
        }

        User user = stored.getUser();

        // Rotate token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newRefresh = createRefreshToken(user);
        String newAccess = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                newAccess,
                newRefresh
        );
    }

    // -------------------------
    // LOGOUT
    // -------------------------
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    // -------------------------
    // HELPER
    // -------------------------
    private String createRefreshToken(User user) {
        String token = java.util.UUID.randomUUID().toString();

        RefreshToken rt = new RefreshToken();
        rt.setToken(token);
        rt.setUser(user);
        rt.setRevoked(false);
        rt.setExpiresAt(LocalDateTime.now().plus(REFRESH_TOKEN_LIFETIME));

        refreshTokenRepository.save(rt);

        return token;
    }
}