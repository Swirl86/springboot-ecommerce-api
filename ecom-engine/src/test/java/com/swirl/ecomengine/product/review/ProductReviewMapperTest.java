package com.swirl.ecomengine.product.review;

import com.swirl.ecomengine.product.dto.review.ProductReviewResponse;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProductReviewMapperTest {

    private final ProductReviewMapper mapper = new ProductReviewMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        Product product = new Product();
        product.setId(1L);

        User user = new User();
        user.setId(5L);
        user.setName("Alice");

        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime lastEditedAt = LocalDateTime.now();

        ProductReview review = TestDataFactory.productReviewWithTimeStamp(
                10L,
                product,
                user,
                5,
                "Great!",
                createdAt,
                lastEditedAt
        );

        // Act
        ProductReviewResponse dto = mapper.toResponse(review);

        // Assert
        assertThat(dto.id()).isEqualTo(10L);
        assertThat(dto.rating()).isEqualTo(5);
        assertThat(dto.comment()).isEqualTo("Great!");
        assertThat(dto.username()).isEqualTo("Alice");
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.lastEditedAt()).isEqualTo(lastEditedAt);
    }

    @Test
    void toResponse_shouldHandleNullTimestamps() {
        // Arrange
        Product product = new Product();
        User user = new User();
        user.setName("Bob");

        ProductReview review = TestDataFactory.productReview(
                20L,
                product,
                user,
                4,
                "Good"
        );

        // Act
        ProductReviewResponse dto = mapper.toResponse(review);

        // Assert
        assertThat(dto.id()).isEqualTo(20L);
        assertThat(dto.rating()).isEqualTo(4);
        assertThat(dto.comment()).isEqualTo("Good");
        assertThat(dto.username()).isEqualTo("Bob");

        assertThat(dto.createdAt()).isNull();
        assertThat(dto.lastEditedAt()).isNull();
    }
}
