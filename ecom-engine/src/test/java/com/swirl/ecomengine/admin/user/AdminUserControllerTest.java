package com.swirl.ecomengine.admin.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.admin.user.controller.AdminUserController;
import com.swirl.ecomengine.admin.user.dto.AdminUserResponse;
import com.swirl.ecomengine.admin.user.service.AdminUserService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.TestDataFactory;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test-controller")
class AdminUserControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AdminUserService service;
    @MockBean private AdminUserMapper mapper;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    private User user;

    @BeforeEach
    void setup() throws Exception {
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        user = TestDataFactory.admin(pwd -> "encoded");
        user.setId(2L);

        Mockito.when(authenticatedUserArgumentResolver.supportsParameter(any()))
                .thenReturn(true);

        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(admin);
    }

    // ---------------------------------------------------------
    // SUCCESS: ADMIN GET ALL USERS
    // ---------------------------------------------------------
    @Test
    void adminCanListAllUsers() throws Exception {
        Mockito.when(service.getAllUsers(any(User.class)))
                .thenReturn(List.of(user));

        Mockito.when(mapper.toResponse(user))
                .thenReturn(new AdminUserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getPhone(),
                        user.getRole(),
                        5
                ));

        mvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // FAIL: SERVICE THROWS EXCEPTION → 500
    // ---------------------------------------------------------
    @Test
    void getAllUsersFailsWhenServiceThrows() throws Exception {
        Mockito.when(service.getAllUsers(any(User.class)))
                .thenThrow(new RuntimeException("DB error"));

        mvc.perform(get("/admin/users"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------------------------------------------------
    // FAIL: MAPPER THROWS EXCEPTION → 500
    // ---------------------------------------------------------
    @Test
    void getAllUsersFailsWhenMapperThrows() throws Exception {
        Mockito.when(service.getAllUsers(any(User.class)))
                .thenReturn(List.of(user));

        Mockito.when(mapper.toResponse(user))
                .thenThrow(new RuntimeException("Mapping failed"));

        mvc.perform(get("/admin/users"))
                .andExpect(status().isInternalServerError());
    }

    // ---------------------------------------------------------
    // FAIL: SERVICE RETURNS EMPTY LIST → STILL 200
    // ---------------------------------------------------------
    @Test
    void getAllUsersReturnsEmptyList() throws Exception {
        Mockito.when(service.getAllUsers(any(User.class)))
                .thenReturn(List.of());

        mvc.perform(get("/admin/users"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // FAIL: SERVICE RETURNS NULL → 500
    // ---------------------------------------------------------
    @Test
    void getAllUsersFailsWhenServiceReturnsNull() throws Exception {
        Mockito.when(service.getAllUsers(any(User.class)))
                .thenReturn(null);

        mvc.perform(get("/admin/users"))
                .andExpect(status().isInternalServerError());
    }
}