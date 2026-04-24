package com.swirl.ecomengine.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.order.dto.UpdateOrderStatusRequest;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import static com.swirl.ecomengine.order.OrderStatus.PROCESSING;
import static com.swirl.ecomengine.order.OrderStatus.SHIPPED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerStatusTest extends IntegrationTestBase {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Autowired private UserRepository userRepository;

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
    }

    // ---------------------------------------------------------
    // ADMIN CAN UPDATE STATUS
    // ---------------------------------------------------------
    @Test
    void adminCanUpdateStatus() throws Exception {
        Order order = orderRepository.save(TestDataFactory.order(user));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(PROCESSING, null);

        mvc.perform(patch("/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
// ADMIN CAN UPDATE STATUS TO SHIPPED
// ---------------------------------------------------------
    @Test
    void adminCanUpdateStatusToShipped() throws Exception {
        Order order = orderRepository.save(TestDataFactory.order(user));
        order.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(order);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(SHIPPED, null);

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

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(PROCESSING, null);

        mvc.perform(patch("/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // USER CANNOT UPDATE SOMEONE ELSE'S ORDER
    // ---------------------------------------------------------
    @Test
    void userCannotUpdateAnotherUsersOrder() throws Exception {
        User otherUser = userRepository.save(TestDataFactory.user(passwordEncoder, "other@example.com"));
        Order order = orderRepository.save(TestDataFactory.order(otherUser));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(PROCESSING, null);

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
        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(PROCESSING, null);

        mvc.perform(patch("/orders/999/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------
    // INVALID REQUEST: NULL STATUS
    // ---------------------------------------------------------
    @Test
    void updateStatusReturns400WhenStatusIsNull() throws Exception {
        Order order = orderRepository.save(TestDataFactory.order(user));

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(null, null);

        mvc.perform(patch("/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // INVALID REQUEST: UNKNOWN STATUS STRING
    // ---------------------------------------------------------
    @Test
    void updateStatusReturns400WhenStatusIsInvalidEnum() throws Exception {
        Order order = orderRepository.save(TestDataFactory.order(user));

        String invalidJson = """
        { "status": "INVALID_STATUS" }
        """;

        mvc.perform(patch("/orders/" + order.getId() + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}