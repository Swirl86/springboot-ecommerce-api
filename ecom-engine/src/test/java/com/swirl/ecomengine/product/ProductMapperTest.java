package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test
    void toResponse_shouldMapAllFields() {
        // ---------------------------------------------------------
        // Arrange
        // ---------------------------------------------------------
        Category category = TestDataFactory.defaultCategory();
        category.setId(10L);

        Product product = TestDataFactory.defaultProduct(category);
        product.setId(1L);

        // ---------------------------------------------------------
        // Act
        // ---------------------------------------------------------
        ProductResponse dto = mapper.toResponse(product);

        // ---------------------------------------------------------
        // Assert
        // ---------------------------------------------------------
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Laptop");
        assertThat(dto.price()).isEqualTo(999.99);
        assertThat(dto.description()).isEqualTo("Powerful laptop");
        assertThat(dto.categoryId()).isEqualTo(10L);
        assertThat(dto.categoryName()).isEqualTo("Electronics");
    }
}