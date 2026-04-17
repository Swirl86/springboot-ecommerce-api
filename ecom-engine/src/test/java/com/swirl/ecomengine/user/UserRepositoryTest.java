package com.swirl.ecomengine.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    // ============================================================
    // existsByEmail
    // ============================================================

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        userRepository.save(
                TestDataFactory.user(pwd -> "encoded", "exists@example.com")
        );

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        assertThat(userRepository.existsByEmail("missing@example.com")).isFalse();
    }

    // ============================================================
    // findByEmail
    // ============================================================

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        userRepository.save(
                TestDataFactory.user(pwd -> "encoded", "findme@example.com")
        );

        assertThat(userRepository.findByEmail("findme@example.com"))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("findme@example.com");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        assertThat(userRepository.findByEmail("missing@example.com")).isNotPresent();
    }

    // ============================================================
    // save (unique email constraint)
    // ============================================================

    @Test
    void save_shouldThrowException_whenEmailIsDuplicate() {
        userRepository.save(
                TestDataFactory.user(pwd -> "encoded", "dup@example.com")
        );

        assertThatThrownBy(() ->
                userRepository.save(
                        TestDataFactory.user(pwd -> "encoded", "dup@example.com")
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}