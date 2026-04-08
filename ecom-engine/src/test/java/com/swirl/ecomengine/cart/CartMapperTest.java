package com.swirl.ecomengine.cart;

import com.swirl.ecomengine.cart.dto.CartItemResponse;
import com.swirl.ecomengine.cart.dto.CartResponse;
import com.swirl.ecomengine.cart.item.CartItem;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CartMapperTest {

    private final CartMapper mapper = new CartMapper();

    @Test
    void toResponse_shouldMapAllFieldsAndCalculateTotal() {
        // ---------------------------------------------------------
        // Arrange
        // ---------------------------------------------------------
        User user = TestDataFactory.user(pwd -> "encoded");
        user.setId(50L);

        Category category = TestDataFactory.defaultCategory();
        category.setId(10L);

        Product laptop = TestDataFactory.product("Laptop", 1000.0, "Gaming laptop", category);
        laptop.setId(1L);

        Product mouse = TestDataFactory.product("Mouse", 50.0, "Wireless mouse", category);
        mouse.setId(2L);

        Cart cart = TestDataFactory.cart(user);
        cart.setId(99L);

        CartItem item1 = TestDataFactory.cartItem(laptop, 1); // total = 1000
        item1.setId(101L);

        CartItem item2 = TestDataFactory.cartItem(mouse, 2); // total = 100
        item2.setId(102L);

        cart.setItems(List.of(item1, item2));

        // ---------------------------------------------------------
        // Act
        // ---------------------------------------------------------
        CartResponse dto = mapper.toResponse(cart);

        // ---------------------------------------------------------
        // Assert
        // ---------------------------------------------------------
        assertThat(dto.id()).isEqualTo(99L);
        assertThat(dto.userId()).isEqualTo(50L);
        assertThat(dto.items()).hasSize(2);

        // Item 1
        CartItemResponse i1 = dto.items().get(0);
        assertThat(i1.id()).isEqualTo(101L);
        assertThat(i1.productId()).isEqualTo(1L);
        assertThat(i1.productName()).isEqualTo("Laptop");
        assertThat(i1.unitPrice()).isEqualTo(1000.0);
        assertThat(i1.quantity()).isEqualTo(1);
        assertThat(i1.totalPrice()).isEqualTo(1000.0);

        // Item 2
        CartItemResponse i2 = dto.items().get(1);
        assertThat(i2.id()).isEqualTo(102L);
        assertThat(i2.productId()).isEqualTo(2L);
        assertThat(i2.productName()).isEqualTo("Mouse");
        assertThat(i2.unitPrice()).isEqualTo(50.0);
        assertThat(i2.quantity()).isEqualTo(2);
        assertThat(i2.totalPrice()).isEqualTo(100.0);

        // Total
        assertThat(dto.total()).isEqualTo(1100.0);
    }
}