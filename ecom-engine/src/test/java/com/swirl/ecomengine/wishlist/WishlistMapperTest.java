package com.swirl.ecomengine.wishlist;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.wishlist.dto.WishlistResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;

class WishlistMapperTest {
    private WishlistMapper mapper;

    private User user;
    private Product product;
    private WishlistItem item;

    @BeforeEach
    void setup() {
        mapper = new WishlistMapper();
        user = TestDataFactory.user(pwd -> "encoded");
        user.setId(100L);

        product = TestDataFactory.defaultProduct(TestDataFactory.defaultCategory());
        product.setId(10L);
        product.getCategory().setId(50L);

        item = new WishlistItem();
        item.setId(1L);
        item.setUser(user);
    }

    @Test
    void toResponse_shouldMapAllFieldsCorrectly() {
        item.setProduct(product);

        WishlistResponse dto = mapper.toResponse(item);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.productId()).isEqualTo(10L);
        assertThat(dto.name()).isEqualTo("Laptop");
        assertThat(dto.price()).isEqualTo(999.99);
        assertThat(dto.description()).isEqualTo("Powerful laptop");
        assertThat(dto.categoryId()).isEqualTo(50L);
    }

    @Test
    void toResponse_handlesNullDescription() {
        product.setDescription(null);
        item.setProduct(product);

        WishlistResponse response = mapper.toResponse(item);

        assertThat(response.description()).isNull();
    }
}