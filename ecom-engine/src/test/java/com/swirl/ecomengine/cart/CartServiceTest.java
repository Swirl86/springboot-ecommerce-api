package com.swirl.ecomengine.cart;

import com.swirl.ecomengine.cart.exception.CartItemNotFoundException;
import com.swirl.ecomengine.cart.exception.CartNotFoundException;
import com.swirl.ecomengine.cart.item.CartItem;
import com.swirl.ecomengine.cart.item.CartItemRepository;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.common.exception.ForbiddenException;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CartServiceTest {

    private CartService cartService;

    private final CartRepository cartRepository = mock(CartRepository.class);
    private final CartItemRepository cartItemRepository = mock(CartItemRepository.class);
    private final ProductRepository productRepository = mock(ProductRepository.class);

    private User user;
    private Product product;

    @BeforeEach
    void setup() {
        cartService = new CartService(cartRepository, cartItemRepository, productRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);
    }

    // ---------------------------------------------------------
    // GET OR CREATE CART
    // ---------------------------------------------------------
    @Test
    void getOrCreateCart_shouldCreateNewCartIfNoneExists() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        cartService.getOrCreateCart(user);

        verify(cartRepository).save(any(Cart.class));
    }

    // ---------------------------------------------------------
    // ADD ITEM
    // ---------------------------------------------------------
    @Test
    void addItem_shouldAddNewItemToCart() {
        Cart cart = new Cart(user);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        cartService.addItem(user, 1L, 2);

        verify(cartItemRepository).save(any(CartItem.class));
        verify(cartRepository).save(cart);
    }

    // ---------------------------------------------------------
    // ADD ITEM (product not found)
    // ---------------------------------------------------------
    @Test
    void addItem_shouldThrowProductNotFound_whenProductDoesNotExist() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(new Cart(user)));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(user, 999L, 2))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 999 not found");
    }

    // ---------------------------------------------------------
    // ADD ITEM (invalid quantity)
    // ---------------------------------------------------------
    @Test
    void addItem_shouldThrowBadRequest_whenQuantityIsInvalid() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(new Cart(user)));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(user, 1L, 0))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Quantity must be at least 1");
    }

    // ---------------------------------------------------------
    // UPDATE ITEM
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldUpdateQuantity() {
        Cart cart = new Cart(user);
        cart.setId(1L);

        CartItem item = new CartItem(product, 1, product.getPrice());
        item.setId(10L);
        item.setCart(cart);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(item));

        cartService.updateItem(user, 10L, 5);

        verify(cartRepository).save(cart);
    }

    // ---------------------------------------------------------
    // UPDATE ITEM (item not found)
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldThrowNotFound_whenItemDoesNotExist() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(new Cart(user)));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateItem(user, 999L, 5))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessage("Cart item with id 999 not found");
    }

    // ---------------------------------------------------------
    // UPDATE ITEM (forbidden)
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldThrowForbidden_whenItemBelongsToAnotherUser() {
        Cart otherUsersCart = new Cart(new User());
        otherUsersCart.setId(2L);

        CartItem item = new CartItem(product, 1, product.getPrice());
        item.setId(10L);
        item.setCart(otherUsersCart);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(new Cart(user)));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> cartService.updateItem(user, 10L, 5))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Item does not belong to this cart");
    }

    // ---------------------------------------------------------
    // REMOVE ITEM
    // ---------------------------------------------------------
    @Test
    void removeItem_shouldRemoveItemFromCart() {
        Cart cart = new Cart(user);
        cart.setId(1L);

        CartItem item = new CartItem(product, 1, product.getPrice());
        item.setId(10L);
        item.setCart(cart);

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findById(10L)).thenReturn(Optional.of(item));

        cartService.removeItem(user, 10L);

        verify(cartItemRepository).delete(item);
        verify(cartRepository).save(cart);
    }

    // ---------------------------------------------------------
    // REMOVE ITEM (item not found)
    // ---------------------------------------------------------
    @Test
    void removeItem_shouldThrowNotFound_whenItemDoesNotExist() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.of(new Cart(user)));
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(user, 999L))
                .isInstanceOf(CartItemNotFoundException.class)
                .hasMessage("Cart item with id 999 not found");
    }

    // ---------------------------------------------------------
    // CLEAR CART
    // ---------------------------------------------------------
    @Test
    void clearCart_shouldRemoveAllItems() {
        Cart cart = new Cart(user);
        cart.setId(1L);
        cart.getItems().add(new CartItem(product, 1, product.getPrice()));

        when(cartRepository.findByUser(user)).thenReturn(Optional.of(cart));

        cartService.clearCart(user);

        verify(cartRepository).save(cart);
    }

    // ---------------------------------------------------------
    // CLEAR CART (cart not found)
    // ---------------------------------------------------------
    @Test
    void clearCart_shouldThrowNotFound_whenCartDoesNotExist() {
        when(cartRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.clearCart(user))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessage("Cart with id 1 not found");
    }
}