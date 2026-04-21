package com.swirl.ecomengine.admin.user;

import com.swirl.ecomengine.admin.user.service.AdminUserService;
import com.swirl.ecomengine.common.exception.UserAccessDeniedException;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testsupport.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AdminUserServiceTest {

    private AdminUserService service;
    private UserRepository userRepository;

    private User admin;
    private User user;

    @BeforeEach
    void setup() {
        userRepository = Mockito.mock(UserRepository.class);
        service = new AdminUserService(userRepository);

        admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        user = TestDataFactory.user(pwd -> "encoded");
        user.setId(2L);
    }

    // ---------------------------------------------------------
    // ADMIN: GET ALL USERS
    // ---------------------------------------------------------
    @Test
    void adminCanGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = service.getAllUsers(admin);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    // ---------------------------------------------------------
    // USER SHOULD GET 403
    // ---------------------------------------------------------
    @Test
    void userGets403WhenTryingToGetAllUsers() {
        assertThrows(UserAccessDeniedException.class, () -> {
            service.getAllUsers(user);
        });
    }

    // ---------------------------------------------------------
    // EMPTY LIST → OK
    // ---------------------------------------------------------
    @Test
    void adminGetsEmptyListWhenNoUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<User> result = service.getAllUsers(admin);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------
    // REPOSITORY THROWS → PROPAGATED
    // ---------------------------------------------------------
    @Test
    void getAllUsersFailsWhenRepositoryThrows() {
        when(userRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> {
            service.getAllUsers(admin);
        });
    }

    // ---------------------------------------------------------
    // ADMIN: GET USER BY ID
    // ---------------------------------------------------------
    @Test
    void adminCanGetUserById() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        User result = service.getUserById(2L, admin);

        assertEquals(2L, result.getId());
    }

    // ---------------------------------------------------------
    // USER SHOULD GET 403 ON GET USER BY ID
    // ---------------------------------------------------------
    @Test
    void userGets403WhenTryingToGetUserById() {
        assertThrows(UserAccessDeniedException.class, () -> {
            service.getUserById(5L, user);
        });
    }

    // ---------------------------------------------------------
    // USER NOT FOUND → RUNTIME EXCEPTION
    // ---------------------------------------------------------
    @Test
    void getUserByIdThrowsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            service.getUserById(99L, admin);
        });
    }

    // ---------------------------------------------------------
    // REPOSITORY THROWS → PROPAGATED
    // ---------------------------------------------------------
    @Test
    void getUserByIdFailsWhenRepositoryThrows() {
        when(userRepository.findById(5L)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> {
            service.getUserById(5L, admin);
        });
    }
}