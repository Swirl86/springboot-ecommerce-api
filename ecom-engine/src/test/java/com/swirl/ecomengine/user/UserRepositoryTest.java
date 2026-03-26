package com.swirl.ecomengine.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByEmail_returnsTrueWhenEmailExists() {
        User user = User.builder()
                .email("exists@example.com")
                .password("pass")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
    }

    @Test
    void findByEmail_returnsUserWhenExists() {
        User user = User.builder()
                .email("findme@example.com")
                .password("pass")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        assertThat(userRepository.findByEmail("findme@example.com"))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("findme@example.com");
    }
}