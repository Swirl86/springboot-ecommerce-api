package com.swirl.ecomengine.order;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.order.dto.OrderItemResponse;
import com.swirl.ecomengine.order.dto.OrderResponse;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toResponse_shouldMapAllFieldsIncludingItems() {
        // ---------------------------------------------------------
        // Arrange
        // ---------------------------------------------------------
        User user = TestDataFactory.user(pwd -> "encoded");
        user.setId(100L);

        Order order = TestDataFactory.order(user);
        order.setId(1L);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Category category = TestDataFactory.defaultCategory();
        category.setId(10L);

        Product product = TestDataFactory.defaultProduct(category);
        product.setId(5L);

        var item = TestDataFactory.orderItem(order, product, 2);
        order.setItems(List.of(item));

        // ---------------------------------------------------------
        // Act
        // ---------------------------------------------------------
        OrderResponse dto = mapper.toResponse(order);

        // ---------------------------------------------------------
        // Assert
        // ---------------------------------------------------------
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.totalPrice()).isEqualTo(order.getTotalPrice());
        assertThat(dto.status()).isEqualTo(order.getStatus());
        assertThat(dto.createdAt()).isEqualTo(order.getCreatedAt());

        assertThat(dto.items()).hasSize(1);

        OrderItemResponse itemDto = dto.items().get(0);
        assertThat(itemDto.productId()).isEqualTo(5L);
        assertThat(itemDto.productName()).isEqualTo(product.getName());
        assertThat(itemDto.price()).isEqualTo(product.getPrice());
        assertThat(itemDto.quantity()).isEqualTo(2);
    }
}