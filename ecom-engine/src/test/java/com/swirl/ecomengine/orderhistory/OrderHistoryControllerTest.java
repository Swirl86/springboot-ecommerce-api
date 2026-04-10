package com.swirl.ecomengine.orderhistory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.orderhistory.controller.OrderHistoryController;
import com.swirl.ecomengine.orderhistory.dto.OrderHistoryResponse;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;
import testsupport.TestDataFactory;
import testsupport.WebMvcTestConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderHistoryController.class)
@Import({SecurityTestConfigMinimal.class, WebMvcTestConfig.class})
@ActiveProfiles("test-controller")
class OrderHistoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderHistoryService historyService;
    @MockBean private OrderHistoryMapper mapper;
    @MockBean private OrderRepository orderRepository;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    private User mockUser;
    private User mockAdmin;

    @BeforeEach
    void setup() throws Exception {
        mockUser = TestDataFactory.user(pwd -> "encoded");
        mockUser.setId(1L);

        mockAdmin = TestDataFactory.admin(pwd -> "encoded");
        mockAdmin.setId(2L);

        Mockito.when(authenticatedUserArgumentResolver.supportsParameter(any()))
                .thenReturn(true);
    }

    // ---------------------------------------------------------
    // USER CAN VIEW OWN HISTORY
    // ---------------------------------------------------------
    @Test
    void getOrderHistory_shouldReturnHistory_whenUserOwnsOrder() throws Exception {
        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);

        Order order = TestDataFactory.order(mockUser);
        order.setId(10L);

        Mockito.when(orderRepository.findById(10L))
                .thenReturn(java.util.Optional.of(order));

        OrderHistoryEntry entry = new OrderHistoryEntry();
        Mockito.when(historyService.getHistory(10L))
                .thenReturn(List.of(entry));

        OrderHistoryResponse dto = new OrderHistoryResponse(
                10L,
                OrderStatus.PENDING,
                OrderStatus.PROCESSING,
                LocalDateTime.now(),
                "admin@example.com"
        );

        Mockito.when(mapper.toResponse(entry)).thenReturn(dto);

        mockMvc.perform(get("/orders/10/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fromStatus").value("PENDING"))
                .andExpect(jsonPath("$[0].toStatus").value("PROCESSING"));
    }

    // ---------------------------------------------------------
    // USER CANNOT VIEW OTHERS HISTORY
    // ---------------------------------------------------------
    @Test
    void getOrderHistory_shouldReturn403_whenUserDoesNotOwnOrder() throws Exception {
        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);

        User other = TestDataFactory.user(pwd -> "encoded");
        other.setId(99L);

        Order order = TestDataFactory.order(other);
        order.setId(10L);

        Mockito.when(orderRepository.findById(10L))
                .thenReturn(java.util.Optional.of(order));

        mockMvc.perform(get("/orders/10/history"))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // ADMIN CAN VIEW ANY HISTORY
    // ---------------------------------------------------------
    @Test
    void getOrderHistory_shouldAllowAdminToViewAnyOrder() throws Exception {
        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockAdmin);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(50L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);

        Mockito.when(orderRepository.findById(10L))
                .thenReturn(java.util.Optional.of(order));

        OrderHistoryEntry entry = new OrderHistoryEntry();
        Mockito.when(historyService.getHistory(10L))
                .thenReturn(List.of(entry));

        Mockito.when(mapper.toResponse(any()))
                .thenReturn(new OrderHistoryResponse(
                        10L,
                        OrderStatus.PENDING,
                        OrderStatus.PROCESSING,
                        LocalDateTime.now(),
                        "admin@example.com"
                ));

        mockMvc.perform(get("/orders/10/history"))
                .andExpect(status().isOk());
    }

    // ---------------------------------------------------------
    // ORDER NOT FOUND
    // ---------------------------------------------------------
    @Test
    void getOrderHistory_shouldReturn404_whenOrderNotFound() throws Exception {
        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);

        Mockito.when(orderRepository.findById(10L))
                .thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/orders/10/history"))
                .andExpect(status().isNotFound());
    }
}