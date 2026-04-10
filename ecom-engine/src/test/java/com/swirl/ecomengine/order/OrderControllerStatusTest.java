package com.swirl.ecomengine.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.order.dto.UpdateOrderStatusRequest;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderControllerStatusTest extends IntegrationTestBase {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Autowired private UserRepository userRepository;

    @Autowired
    @Qualifier("jwtUserRepository")
    private UserRepository mockUserRepository;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private OrderRepository orderRepository;

    private String adminToken;
    private String userToken;

    private User user;

    @BeforeEach
    void setup() {
        // Create ADMIN
        User admin = userRepository.save(TestDataFactory.admin(passwordEncoder));
        adminToken = jwtService.generateToken(admin);

        // Create USER
        user = userRepository.save(TestDataFactory.user(passwordEncoder));
        userToken = jwtService.generateToken(user);

        // Mock JWT filter lookup
        when(mockUserRepository.findByEmail(admin.getEmail())).thenReturn(Optional.of(admin));
        when(mockUserRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    // ---------------------------------------------------------
    // ADMIN CAN UPDATE STATUS
    // ---------------------------------------------------------
    @Test
    void adminCanUpdateStatus() throws Exception {
        Order order = orderRepository.save(TestDataFactory.order(user));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);

        mvc.perform(patch("/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // USER CANNOT UPDATE STATUS
    // ---------------------------------------------------------
    @Test
    void userCannotUpdateStatus() throws Exception {
        Order order = orderRepository.save(TestDataFactory.order(user));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);

        mvc.perform(patch("/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // ORDER NOT FOUND
    // ---------------------------------------------------------
    @Test
    void updateStatusReturns404WhenOrderNotFound() throws Exception {
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.PROCESSING);

        mvc.perform(patch("/orders/999/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}