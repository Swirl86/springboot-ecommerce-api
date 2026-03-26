package com.swirl.ecomengine.user;

import com.swirl.ecomengine.common.exception.EmailAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void createUser_successfullyCreatesUser() {
        UserService service = new UserService(userRepository, encoder);

        User user = service.createUser("test@example.com", "password123");

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(encoder.matches("password123", user.getPassword())).isTrue();
        assertThat(user.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void createUser_emailAlreadyExists_throwsException() {
        User existing = User.builder()
                .email("test@example.com")
                .password("hashed")
                .role(Role.USER)
                .build();

        userRepository.save(existing);

        UserService service = new UserService(userRepository, encoder);

        assertThatThrownBy(() ->
                service.createUser("test@example.com", "password123")
        ).isInstanceOf(EmailAlreadyExistsException.class);
    }
}