package com.swirl.ecomengine.order;

import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.order.exception.OrderBadRequestException;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.order.service.OrderService;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class OrderServiceStatusTest {

    private OrderRepository orderRepository;
    private OrderHistoryService historyService;
    private OrderService service;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        historyService = mock(OrderHistoryService.class);
        CartService cartService = mock(CartService.class);

        service = new OrderService(cartService, orderRepository, historyService);
    }

    // ---------------------------------------------------------
    // VALID TRANSITION: PENDING → PROCESSING
    // ---------------------------------------------------------
    @Test
    void updateStatus_shouldAllowPendingToProcessing() {
        // Arrange
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(2L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        Order result = service.updateStatus(10L, OrderStatus.PROCESSING, admin);

        // Assert
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(historyService).logStatusChange(order, OrderStatus.PENDING, OrderStatus.PROCESSING, admin);
    }

    // ---------------------------------------------------------
    // INVALID TRANSITION: COMPLETED → PROCESSING
    // ---------------------------------------------------------
    @Test
    void updateStatus_shouldRejectInvalidTransition() {
        // Arrange
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(2L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);
        order.setStatus(OrderStatus.COMPLETED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        // Act + Assert
        assertThatThrownBy(() -> service.updateStatus(10L, OrderStatus.PROCESSING, admin))
                .isInstanceOf(OrderBadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    // ---------------------------------------------------------
    // ORDER NOT FOUND
    // ---------------------------------------------------------
    @Test
    void updateStatus_shouldThrowWhenOrderNotFound() {
        User admin = TestDataFactory.admin(pwd -> "encoded");

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(99L, OrderStatus.PROCESSING, admin))
                .isInstanceOf(OrderNotFoundException.class);
    }
}