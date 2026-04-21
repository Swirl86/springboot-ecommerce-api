package com.swirl.ecomengine.admin.order;

import com.swirl.ecomengine.admin.order.service.AdminOrderService;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.exception.OrderAccessDeniedException;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import testsupport.TestDataFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AdminOrderServiceTest {

    private AdminOrderService service;
    private OrderRepository orderRepository;

    private User admin;
    private User user;

    @BeforeEach
    void setup() {
        orderRepository = Mockito.mock(OrderRepository.class);
        OrderHistoryService historyService = Mockito.mock(OrderHistoryService.class);

        service = new AdminOrderService(orderRepository, historyService);

        admin = TestDataFactory.admin(pwd -> "encoded");
        admin.setId(1L);

        user = TestDataFactory.user(pwd -> "encoded");
        user.setId(2L);
    }

    // ---------------------------------------------------------
    // SUCCESS: ADMIN GET ALL ORDERS
    // ---------------------------------------------------------
    @Test
    void adminCanGetAllOrders() {
        Order order = new Order();
        order.setId(10L);

        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> result = service.getAllOrders(admin);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
    }

    @Test
    void getAllOrdersReturnsEmptyListWhenNoOrdersExist() {
        when(orderRepository.findAll()).thenReturn(List.of());

        List<Order> result = service.getAllOrders(admin);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------
    // FAIL: USER SHOULD GET 403
    // ---------------------------------------------------------
    @Test
    void userGets403WhenTryingToGetAllOrders() {
        assertThrows(OrderAccessDeniedException.class, () -> {
            service.getAllOrders(user);
        });
    }

    // ---------------------------------------------------------
    // FAIL: SERVICE THROWS RUNTIME EXCEPTION
    // ---------------------------------------------------------
    @Test
    void getAllOrdersFailsWhenRepositoryThrows() {
        when(orderRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> {
            service.getAllOrders(admin);
        });
    }
}