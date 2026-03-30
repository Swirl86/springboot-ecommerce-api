package com.swirl.ecomengine.user;

import com.swirl.ecomengine.user.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        userService = new UserService(userRepository, encoder);
    }

    // ============================================================
    // createUser
    // ============================================================

    @Test
    void createUser_shouldPersistUserWithEncodedPassword_andDefaultRole() {
        User user = userService.createUser("test@example.com", "password123");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(encoder.matches("password123", user.getPassword())).isTrue();
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        userRepository.save(User.builder()
                .email("test@example.com")
                .password("hashed")
                .role(Role.USER)
                .build());

        assertThatThrownBy(() ->
                userService.createUser("test@example.com", "password123")
        ).isInstanceOf(EmailAlreadyExistsException.class);
    }
}