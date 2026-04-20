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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;
import testsupport.TestDataFactory;
import testsupport.WebMvcTestConfig;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private User user;

    @BeforeEach
    void setup() throws Exception {
        user = TestDataFactory.user(pwd -> "encoded");
        user.setId(1L);

        Mockito.when(authenticatedUserArgumentResolver.supportsParameter(
                argThat(param -> param.getParameterType().equals(User.class))
        )).thenReturn(true);

        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(
                any(), any(), any(), any()
        )).thenReturn(user);
    }

    // ---------------------------------------------------------
    // CHECKOUT (POST /orders/checkout)
    // ---------------------------------------------------------
    @Test
    void checkout_shouldReturnCreatedOrder() throws Exception {
        Order order = TestDataFactory.order(user);
        order.setId(100L);
        order.setTotalPrice(199.99);

        OrderResponse response = new OrderResponse(
                100L,
                199.99,
                order.getStatus(),
                order.getCreatedAt(),
                List.of()
        );

        Mockito.when(orderService.placeOrder(user)).thenReturn(order);
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
        Mockito.when(orderService.placeOrder(user))
                .thenThrow(new OrderBadRequestException("Cannot place order with empty cart"));

        mockMvc.perform(post("/orders/checkout"))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // GET ALL ORDERS (GET /orders)
    // ---------------------------------------------------------
    @Test
    void getOrders_shouldReturnPaginatedOrders() throws Exception {
        Order order = TestDataFactory.order(user);
        order.setId(200L);
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalPrice(150.00);

        OrderResponse response = new OrderResponse(
                200L,
                150.00,
                OrderStatus.COMPLETED,
                order.getCreatedAt(),
                List.of()
        );

        Pageable pageable = PageRequest.of(0, 20);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 1);

        Mockito.when(orderService.getOrderHistory(eq(user), any(Pageable.class)))
                .thenReturn(page);

        Mockito.when(orderMapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(200L))
                .andExpect(jsonPath("$.content[0].totalPrice").value(150.00))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    // ---------------------------------------------------------
    // GET ORDER BY ID
    // ---------------------------------------------------------
    @Test
    void getOrderById_shouldReturnOrder_whenExists() throws Exception {
        Order order = TestDataFactory.order(user);
        order.setId(300L);
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalPrice(300.00);

        OrderResponse response = new OrderResponse(
                300L,
                300.00,
                OrderStatus.COMPLETED,
                order.getCreatedAt(),
                List.of()
        );

        Mockito.when(orderService.getOrderById(user, 300L)).thenReturn(order);
        Mockito.when(orderMapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/orders/300"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(300L));
    }

    @Test
    void getOrderById_shouldReturn404_whenNotFound() throws Exception {
        Mockito.when(orderService.getOrderById(user, 999L))
                .thenThrow(new OrderNotFoundException(999L));

        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderById_shouldReturn403_whenAccessDenied() throws Exception {
        Mockito.when(orderService.getOrderById(user, 5L))
                .thenThrow(new OrderAccessDeniedException());

        mockMvc.perform(get("/orders/5"))
                .andExpect(status().isForbidden());
    }

    // ---------------------------------------------------------
    // GET ALL ORDERS (pagination)
    // ---------------------------------------------------------
    @Test
    void getOrders_shouldRespectPageAndSizeParameters() throws Exception {
        Order order = TestDataFactory.order(user);
        order.setId(201L);
        order.setStatus(OrderStatus.COMPLETED);
        order.setTotalPrice(99.99);

        OrderResponse response = new OrderResponse(
                201L,
                99.99,
                OrderStatus.COMPLETED,
                order.getCreatedAt(),
                List.of()
        );

        Pageable pageable = PageRequest.of(2, 5);
        Page<Order> page = new PageImpl<>(List.of(order), pageable, 30);

        Mockito.when(orderService.getOrderHistory(eq(user), any(Pageable.class)))
                .thenReturn(page);

        Mockito.when(orderMapper.toResponse(order)).thenReturn(response);

        mockMvc.perform(get("/orders?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(201L))
                .andExpect(jsonPath("$.pageable.pageNumber").value(2))
                .andExpect(jsonPath("$.pageable.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.totalPages").value(6));
    }

    @Test
    void getOrders_shouldReturnEmptyPage_whenNoOrdersExist() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Order> emptyPage = Page.empty(pageable);

        Mockito.when(orderService.getOrderHistory(eq(user), any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(20));
    }

    // ---------------------------------------------------------
    // DELETE ORDER
    // ---------------------------------------------------------
    @Test
    void deleteOrder_shouldReturn204_whenSuccessful() throws Exception {
        Mockito.doNothing().when(orderService).delete(10L);

        mockMvc.perform(delete("/orders/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteOrder_shouldReturn404_whenOrderNotFound() throws Exception {
        Mockito.doThrow(new OrderNotFoundException(999L))
                .when(orderService).delete(999L);

        mockMvc.perform(delete("/orders/999"))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------
    // RESTORE ORDER
    // ---------------------------------------------------------
    @Test
    void restoreOrder_shouldReturn204_whenSuccessful() throws Exception {
        Mockito.doNothing().when(orderService).restore(20L, user);

        mockMvc.perform(post("/orders/20/restore"))
                .andExpect(status().isNoContent());
    }

    @Test
    void restoreOrder_shouldReturn404_whenOrderNotFound() throws Exception {
        Mockito.doThrow(new OrderNotFoundException(123L))
                .when(orderService).restore(123L, user);

        mockMvc.perform(post("/orders/123/restore"))
                .andExpect(status().isNotFound());
    }

    @Test
    void restoreOrder_shouldReturn403_whenAccessDenied() throws Exception {
        Mockito.doThrow(new OrderAccessDeniedException())
                .when(orderService).restore(5L, user);

        mockMvc.perform(post("/orders/5/restore"))
                .andExpect(status().isForbidden());
    }
}