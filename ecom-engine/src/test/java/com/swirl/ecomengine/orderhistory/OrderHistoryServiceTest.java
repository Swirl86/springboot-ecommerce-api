package com.swirl.ecomengine.orderhistory;

import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    // GET HISTORY (PAGINATED)
    // ---------------------------------------------------------
    @Test
    void getHistory_shouldReturnPaginatedEntries() {
        OrderHistoryEntry e1 = new OrderHistoryEntry();
        OrderHistoryEntry e2 = new OrderHistoryEntry();

        Pageable pageable = PageRequest.of(0, 20);
        Page<OrderHistoryEntry> page = new PageImpl<>(List.of(e1, e2), pageable, 2);

        when(repository.findByOrderId(10L, pageable))
                .thenReturn(page);

        Page<OrderHistoryEntry> result = service.getHistory(10L, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    // ---------------------------------------------------------
    // GET HISTORY SHOULD PASS PAGEABLE TO REPOSITORY
    // ---------------------------------------------------------
    @Test
    void getHistory_shouldPassPageableToRepository() {
        Pageable pageable = PageRequest.of(2, 50); // page=2, size=50

        Page<OrderHistoryEntry> emptyPage = Page.empty(pageable);
        when(repository.findByOrderId(10L, pageable)).thenReturn(emptyPage);

        service.getHistory(10L, pageable);

        verify(repository).findByOrderId(10L, pageable);
    }
}