package com.swirl.ecomengine.user;

import com.swirl.ecomengine.address.AddressRepository;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.user.exception.UserNotFoundException;
import com.swirl.ecomengine.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import testsupport.TestDataFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        AddressRepository addressRepository = Mockito.mock(AddressRepository.class);
        PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);

        userService = new UserService(userRepository, orderRepository, addressRepository, passwordEncoder);
    }

    // ============================================================
    // getById
    // ============================================================

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        User saved = userRepository.save(TestDataFactory.user(pwd -> "encoded"));

        User result = userService.getById(saved.getId());

        assertThat(result.getEmail()).isEqualTo("user@example.com");
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
        userRepository.save(TestDataFactory.user(pwd -> "encoded", "a@example.com"));
        userRepository.save(TestDataFactory.user(pwd -> "encoded", "b@example.com"));

        List<User> users = userService.getAll();

        assertThat(users).hasSize(2);
    }

    // ============================================================
    // update
    // ============================================================

    @Test
    void update_shouldPersistUpdatedUser() {
        User saved = userRepository.save(TestDataFactory.user(pwd -> "encoded", "old@example.com"));

        saved.setEmail("new@example.com");

        User updated = userService.update(saved);

        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }
}