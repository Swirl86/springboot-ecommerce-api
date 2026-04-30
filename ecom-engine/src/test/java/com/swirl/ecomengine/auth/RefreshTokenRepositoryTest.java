package com.swirl.ecomengine.auth;

import com.swirl.ecomengine.auth.repository.RefreshTokenRepository;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import testsupport.TestDataFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    // ============================================================
    // save + findByToken
    // ============================================================

    @Test
    void save_and_findByToken_shouldPersistAndRetrieveRefreshToken() {
        User user = TestDataFactory.userNoPassword("test@example.com");
        userRepository.save(user);

        RefreshToken token = TestDataFactory.refreshToken(
                user,
                "abc123",
                LocalDateTime.now().plusDays(7),
                false
        );

        refreshTokenRepository.save(token);

        RefreshToken found = refreshTokenRepository.findByToken("abc123").orElseThrow();

        assertThat(found.getToken()).isEqualTo("abc123");
        assertThat(found.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(found.isRevoked()).isFalse();
    }

    // ============================================================
    // revoked flag persists
    // ============================================================

    @Test
    void save_shouldPersistRevokedFlag() {
        User user = TestDataFactory.userNoPassword("revoked@example.com");
        userRepository.save(user);

        RefreshToken token = TestDataFactory.refreshToken(
                user,
                "revoked-token",
                LocalDateTime.now().plusDays(7),
                true
        );

        refreshTokenRepository.save(token);

        RefreshToken found = refreshTokenRepository.findByToken("revoked-token").orElseThrow();

        assertThat(found.isRevoked()).isTrue();
    }

    // ============================================================
    // expiresAt persists
    // ============================================================

    @Test
    void save_shouldPersistExpirationDate() {
        User user = TestDataFactory.userNoPassword("expires@example.com");
        userRepository.save(user);

        LocalDateTime expiry = LocalDateTime.now().plusDays(3);

        RefreshToken token = TestDataFactory.refreshToken(
                user,
                "expiry-token",
                expiry,
                false
        );

        refreshTokenRepository.save(token);

        RefreshToken found = refreshTokenRepository.findByToken("expiry-token").orElseThrow();

        assertThat(found.getExpiresAt()).isEqualTo(expiry);
    }
}