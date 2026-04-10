package com.swirl.ecomengine.orderhistory;

import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderHistoryServiceTest {

    private OrderHistoryRepository repository;
    private OrderHistoryService service;

    @BeforeEach
    void setup() {
        repository = mock(OrderHistoryRepository.class);
        service = new OrderHistoryService(repository);
    }

    // ---------------------------------------------------------
    // LOG STATUS CHANGE
    // ---------------------------------------------------------
    @Test
    void logStatusChange_shouldSaveEntry() {
        Order order = new Order();
        order.setId(10L);

        User admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@test.com");

        service.logStatusChange(order, OrderStatus.PENDING, OrderStatus.PROCESSING, admin);

        verify(repository).save(any(OrderHistoryEntry.class));
    }

    // ---------------------------------------------------------
    // GET HISTORY
    // ---------------------------------------------------------
    @Test
    void getHistory_shouldReturnEntries() {
        OrderHistoryEntry e1 = new OrderHistoryEntry();
        OrderHistoryEntry e2 = new OrderHistoryEntry();

        when(repository.findByOrderIdOrderByChangedAtAsc(10L))
                .thenReturn(List.of(e1, e2));

        List<OrderHistoryEntry> result = service.getHistory(10L);

        assertThat(result).hasSize(2);
    }
}