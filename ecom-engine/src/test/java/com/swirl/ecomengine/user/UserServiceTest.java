package com.swirl.ecomengine.user;

import com.swirl.ecomengine.user.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        userService = new UserService(userRepository);
    }

    // ============================================================
    // getById
    // ============================================================

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        User saved = userRepository.save(
                User.builder()
                        .email("test@example.com")
                        .password("hashed")
                        .role(Role.USER)
                        .build()
        );

        User result = userService.getById(saved.getId());

        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getById_shouldThrowException_whenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ============================================================
    // getAll
    // ============================================================

    @Test
    void getAll_shouldReturnAllUsers() {
        userRepository.save(User.builder().email("a@example.com").password("x").role(Role.USER).build());
        userRepository.save(User.builder().email("b@example.com").password("y").role(Role.USER).build());

        List<User> users = userService.getAll();

        assertThat(users).hasSize(2);
    }

    // ============================================================
    // update
    // ============================================================

    @Test
    void update_shouldPersistUpdatedUser() {
        User saved = userRepository.save(
                User.builder()
                        .email("old@example.com")
                        .password("hashed")
                        .role(Role.USER)
                        .build()
        );

        saved.setEmail("new@example.com");

        User updated = userService.update(saved);

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }
}