package com.swirl.ecomengine.orderhistory;

import com.swirl.ecomengine.orderhistory.dto.OrderHistoryResponse;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.time.LocalDateTime;

import static com.swirl.ecomengine.order.OrderStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class OrderHistoryMapperTest {

    private final OrderHistoryMapper mapper = new OrderHistoryMapper();

    // ---------------------------------------------------------
    // MAP FIELDS CORRECTLY
    // ---------------------------------------------------------
    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        // Arrange
        User admin = TestDataFactory.admin(pwd -> "encoded");

        LocalDateTime now = LocalDateTime.now();

        OrderHistoryEntry entry = OrderHistoryEntry.builder()
                .id(5L)
                .fromStatus(PENDING)
                .toStatus(PROCESSING)
                .changedAt(now)
                .changedBy(admin)
                .build();

        // Act
        OrderHistoryResponse dto = mapper.toResponse(entry);

        // Assert
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.fromStatus()).isEqualTo(PENDING);
        assertThat(dto.toStatus()).isEqualTo(PROCESSING);
        assertThat(dto.changedAt()).isEqualTo(now);
        assertThat(dto.changedByEmail()).isEqualTo(admin.getEmail());
    }

    // ---------------------------------------------------------
    // HANDLE NULL changedBy
    // ---------------------------------------------------------
    @Test
    void toResponse_shouldHandleNullChangedBy() {
        LocalDateTime now = LocalDateTime.now();

        OrderHistoryEntry entry = OrderHistoryEntry.builder()
                .id(7L)
                .fromStatus(PROCESSING)
                .toStatus(SHIPPED)
                .changedAt(now)
                .changedBy(null)
                .build();

        OrderHistoryResponse dto = mapper.toResponse(entry);

        assertThat(dto.changedByEmail()).isEqualTo("system");
    }

    // ---------------------------------------------------------
    // MAP changedAt CORRECTLY
    // ---------------------------------------------------------
    @Test
    void toResponse_shouldMapChangedAtCorrectly() {
        LocalDateTime timestamp = LocalDateTime.now();

        OrderHistoryEntry entry = OrderHistoryEntry.builder()
                .id(8L)
                .fromStatus(SHIPPED)
                .toStatus(COMPLETED)
                .changedAt(timestamp)
                .changedBy(null)
                .build();

        OrderHistoryResponse dto = mapper.toResponse(entry);

        assertThat(dto.changedAt()).isEqualTo(timestamp);
    }
}