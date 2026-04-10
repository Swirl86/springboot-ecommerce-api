package com.swirl.ecomengine.orderhistory;

import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.orderhistory.dto.OrderHistoryResponse;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrderHistoryMapperTest {

    private final OrderHistoryMapper mapper = new OrderHistoryMapper();

    @Test
    void toResponse_shouldMapFieldsCorrectly() {
        // Arrange
        User admin = new User();
        admin.setEmail("admin@test.com");

        OrderHistoryEntry entry = OrderHistoryEntry.builder()
                .id(5L)
                .fromStatus(OrderStatus.PENDING)
                .toStatus(OrderStatus.PROCESSING)
                .changedAt(LocalDateTime.now())
                .changedBy(admin)
                .build();

        // Act
        OrderHistoryResponse dto = mapper.toResponse(entry);

        // Assert
        assertThat(dto.id()).isEqualTo(5L);
        assertThat(dto.fromStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(dto.toStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(dto.changedByEmail()).isEqualTo("admin@test.com");
    }
}