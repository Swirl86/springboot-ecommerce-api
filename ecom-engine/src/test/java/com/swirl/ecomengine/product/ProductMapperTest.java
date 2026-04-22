package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    // ---------------------------------------------------------
    // MAP FULL PRODUCT
    // ---------------------------------------------------------
    @Test
    void toResponse_shouldMapAllFields() {
        Category category = TestDataFactory.defaultCategory();
        category.setId(10L);

        Product product = TestDataFactory.defaultProduct(category);
        product.setId(1L);

        ProductResponse dto = mapper.toResponse(product);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo(product.getName());
        assertThat(dto.price()).isEqualTo(product.getPrice());
        assertThat(dto.description()).isEqualTo(product.getDescription());
        assertThat(dto.categoryId()).isEqualTo(10L);
        assertThat(dto.categoryName()).isEqualTo(category.getName());
        assertThat(dto.imageUrls()).isNotNull();
        assertThat(dto.imageUrls()).hasSize(1);
        assertThat(dto.imageUrls().get(0)).isEqualTo("https://example.com/laptop.jpg");
    }

    // ---------------------------------------------------------
    // MAP PRODUCT WITH NULL DESCRIPTION
    // ---------------------------------------------------------
    @Test
    void toResponse_shouldHandleNullDescription() {
        Category category = TestDataFactory.defaultCategory();
        category.setId(10L);

        Product product = TestDataFactory.product("NoDesc", 100, null, category);
        product.setId(2L);

        ProductResponse dto = mapper.toResponse(product);

        assertThat(dto.id()).isEqualTo(2L);
        assertThat(dto.description()).isNull();
    }
}