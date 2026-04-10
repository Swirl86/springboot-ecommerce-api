package com.swirl.ecomengine.orderhistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class OrderHistoryIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderHistoryRepository historyRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private String adminToken;
    private Order order;

    @BeforeEach
    void setup() {
        User user = userRepository.save(TestDataFactory.user(passwordEncoder));
        User admin = userRepository.save(TestDataFactory.admin(passwordEncoder));

        userToken = jwtService.generateToken(user);
        adminToken = jwtService.generateToken(admin);

        order = orderRepository.save(TestDataFactory.order(user));

        historyRepository.save(OrderHistoryEntry.builder()
                .order(order)
                .fromStatus(OrderStatus.PENDING)
                .toStatus(OrderStatus.PROCESSING)
                .changedAt(LocalDateTime.now())
                .changedBy(admin)
                .build());
    }

    // ---------------------------------------------------------
    // USER CAN VIEW OWN HISTORY
    // ---------------------------------------------------------
    @Test
    void user_canViewOwnOrderHistory() throws Exception {
        mockMvc.perform(get("/orders/" + order.getId() + "/history")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].toStatus").value("PROCESSING"));
    }

    // ---------------------------------------------------------
    // USER CANNOT VIEW OTHERS HISTORY
    // ---------------------------------------------------------
    @Test
    void user_cannotViewOthersOrderHistory() throws Exception {
        User other = userRepository.save(TestDataFactory.user(passwordEncoder, "other@example.com"));
        String otherToken = jwtService.generateToken(other);

        mockMvc.perform(get("/orders/" + order.getId() + "/history")
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // ADMIN CAN VIEW ANY HISTORY
    // ---------------------------------------------------------
    @Test
    void admin_canViewAnyOrderHistory() throws Exception {
        mockMvc.perform(get("/orders/" + order.getId() + "/history")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}