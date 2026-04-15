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

import static com.swirl.ecomengine.order.OrderStatus.*;
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
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(2L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);
        order.setStatus(PENDING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = service.updateStatus(10L, PROCESSING, admin, null);

        assertThat(result.getStatus()).isEqualTo(PROCESSING);
        verify(historyService).logStatusChange(order, PENDING, PROCESSING, admin, null);
    }

    // ---------------------------------------------------------
    // VALID TRANSITION: PROCESSING → SHIPPED
    // ---------------------------------------------------------
    @Test
    void updateStatus_shouldAllowProcessingToShipped() {
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(2L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);
        order.setStatus(PROCESSING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        Order result = service.updateStatus(10L, SHIPPED, admin, null);

        assertThat(result.getStatus()).isEqualTo(SHIPPED);
        verify(historyService).logStatusChange(order, PROCESSING, SHIPPED, admin, null);
    }

    // ---------------------------------------------------------
    // INVALID TRANSITION: PROCESSING → COMPLETED
    // ---------------------------------------------------------
    @Test
    void updateStatus_shouldRejectProcessingToCompleted() {
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(2L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);
        order.setStatus(PROCESSING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateStatus(10L, COMPLETED, admin, null))
                .isInstanceOf(OrderBadRequestException.class)
                .hasMessageContaining("Invalid status transition");
    }

    // ---------------------------------------------------------
    // INVALID TRANSITION: COMPLETED → PROCESSING
    // ---------------------------------------------------------
    @Test
    void updateStatus_shouldRejectInvalidTransition() {
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(2L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);
        order.setStatus(COMPLETED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.updateStatus(10L, PROCESSING, admin, null))
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

        assertThatThrownBy(() -> service.updateStatus(99L, PROCESSING, admin, null))
                .isInstanceOf(OrderNotFoundException.class);
    }

    // ---------------------------------------------------------
    // REASON: PENDING → PROCESSING with reason passed through
    // ---------------------------------------------------------
    @Test
    void updateStatus_shouldPassReasonToHistoryService() {
        User admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        User owner = TestDataFactory.user(pwd -> "encoded");
        owner.setId(2L);

        Order order = TestDataFactory.order(owner);
        order.setId(10L);
        order.setStatus(PENDING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        String reason = "Admin approved shipment";

        service.updateStatus(10L, PROCESSING, admin, reason);

        verify(historyService).logStatusChange(order, PENDING, PROCESSING, admin, reason);
    }
}