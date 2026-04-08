package com.swirl.ecomengine.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.order.controller.OrderController;
import com.swirl.ecomengine.order.dto.OrderResponse;
import com.swirl.ecomengine.order.exception.OrderAccessDeniedException;
import com.swirl.ecomengine.order.exception.OrderBadRequestException;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.order.service.OrderService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;
import testsupport.WebMvcTestConfig;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import({SecurityTestConfigMinimal.class, WebMvcTestConfig.class})
@ActiveProfiles("test-controller")
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private OrderService orderService;
    @MockBean private OrderMapper orderMapper;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    private User mockUser;

    @BeforeEach
    void setup() throws Exception {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");

        Mockito.when(authenticatedUserArgumentResolver.supportsParameter(any()))
                .thenReturn(true);

        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);
    }

    // ---------------------------------------------------------
    // CHECKOUT (POST /orders/checkout)
    // ---------------------------------------------------------
    @Test
    void checkout_shouldReturnCreatedOrder() throws Exception {
        Order order = Order.builder()
                .id(100L)
                .user(mockUser)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of())
                .totalPrice(199.99)
                .build();

        OrderResponse response = new OrderResponse(
                100L,
                199.99,
                OrderStatus.PENDING,
                order.getCreatedAt(),
                List.of()
        );

        Mockito.when(orderService.placeOrder(mockUser)).thenReturn(order);
        Mockito.when(orderMapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(post("/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.totalPrice").value(199.99))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void checkout_shouldReturn400_whenCartIsEmpty() throws Exception {
        Mockito.when(orderService.placeOrder(mockUser))
                .thenThrow(new OrderBadRequestException("Cannot place order with empty cart"));

        mockMvc.perform(post("/orders/checkout"))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // GET ALL ORDERS (GET /orders)
    // ---------------------------------------------------------
    @Test
    void getOrders_shouldReturnListOfOrders() throws Exception {
        Order order = Order.builder()
                .id(200L)
                .user(mockUser)
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of())
                .totalPrice(150.00)
                .build();

        OrderResponse response = new OrderResponse(
                200L,
                150.00,
                OrderStatus.COMPLETED,
                order.getCreatedAt(),
                List.of()
        );

        Mockito.when(orderService.getOrderHistory(mockUser)).thenReturn(List.of(order));
        Mockito.when(orderMapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(200L))
                .andExpect(jsonPath("$[0].totalPrice").value(150.00));
    }

    // ---------------------------------------------------------
    // GET ORDER BY ID (GET /orders/{id})
    // ---------------------------------------------------------
    @Test
    void getOrderById_shouldReturnOrder_whenExists() throws Exception {
        Order order = Order.builder()
                .id(300L)
                .user(mockUser)
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .items(List.of())
                .totalPrice(300.00)
                .build();

        OrderResponse response = new OrderResponse(
                300L,
                300.00,
                OrderStatus.COMPLETED,
                order.getCreatedAt(),
                List.of()
        );

        Mockito.when(orderService.getOrderById(mockUser, 300L)).thenReturn(order);
        Mockito.when(orderMapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/orders/300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(300L));
    }

    @Test
    void getOrderById_shouldReturn404_whenNotFound() throws Exception {
        Mockito.when(orderService.getOrderById(mockUser, 999L))
                .thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderById_shouldReturn403_whenAccessDenied() throws Exception {
        Mockito.when(orderService.getOrderById(mockUser, 5L))
                .thenThrow(new OrderAccessDeniedException());

        mockMvc.perform(get("/orders/5"))
                .andExpect(status().isForbidden());
    }
}