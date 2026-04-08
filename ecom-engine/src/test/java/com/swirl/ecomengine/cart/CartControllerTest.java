package com.swirl.ecomengine.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.cart.controller.CartController;
import com.swirl.ecomengine.cart.dto.CartItemRequest;
import com.swirl.ecomengine.cart.dto.CartItemResponse;
import com.swirl.ecomengine.cart.dto.CartItemUpdateRequest;
import com.swirl.ecomengine.cart.dto.CartResponse;
import com.swirl.ecomengine.cart.exception.CartItemNotFoundException;
import com.swirl.ecomengine.cart.exception.CartNotFoundException;
import com.swirl.ecomengine.cart.item.CartItem;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.common.exception.ForbiddenException;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CartService cartService;
    @MockBean private CartMapper cartMapper;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    private User mockUser;

    @BeforeEach
    void setup() throws Exception {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@example.com");

        Mockito.when(authenticatedUserArgumentResolver.supportsParameter(any())).thenReturn(true);
        Mockito.when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(mockUser);
    }

    // ---------------------------------------------------------
    // ADD ITEM
    // ---------------------------------------------------------
    @Test
    void addItem_shouldReturnUpdatedCart() throws Exception {
        CartItemRequest request = new CartItemRequest(1L, 2);

        // Mock Cart entity
        Cart cart = new Cart(mockUser);
        cart.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);

        CartItem item = new CartItem(product, 2, 999.99);
        item.setId(10L);
        item.setCart(cart);
        cart.getItems().add(item);

        // Mock service
        Mockito.when(cartService.addItem(mockUser, 1L, 2)).thenReturn(cart);

        // Mock mapper
        CartItemResponse itemResponse = new CartItemResponse(10L, 1L, "Laptop", 999.99, 2, 1999.98);
        CartResponse response = new CartResponse(1L, 1L, List.of(itemResponse), 1999.98);

        Mockito.when(cartMapper.toResponse(cart)).thenReturn(response);

        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    // ---------------------------------------------------------
    // ADD ITEM (product not found → 404)
    // ---------------------------------------------------------
    @Test
    void addItem_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        CartItemRequest request = new CartItemRequest(999L, 2);

        Mockito.when(cartService.addItem(mockUser, 999L, 2))
                .thenThrow(new ProductNotFoundException(999L));

        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id 999 not found"));
    }

    // ---------------------------------------------------------
    // ADD ITEM (invalid quantity → 400)
    // ---------------------------------------------------------
    @Test
    void addItem_shouldReturnBadRequest_whenQuantityIsInvalid() throws Exception {
        CartItemRequest request = new CartItemRequest(1L, 0);

        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // UPDATE ITEM
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldReturnUpdatedCart() throws Exception {
        CartItemUpdateRequest request = new CartItemUpdateRequest(5);

        Cart cart = new Cart(mockUser);
        cart.setId(1L);

        Product product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setPrice(999.99);

        CartItem item = new CartItem(product, 5, 999.99);
        item.setId(10L);
        item.setCart(cart);
        cart.getItems().add(item);

        Mockito.when(cartService.updateItem(mockUser, 10L, 5)).thenReturn(cart);

        CartItemResponse itemResponse = new CartItemResponse(10L, 1L, "Laptop", 999.99, 5, 4999.95);
        CartResponse response = new CartResponse(1L, 1L, List.of(itemResponse), 4999.95);

        Mockito.when(cartMapper.toResponse(cart)).thenReturn(response);

        mockMvc.perform(put("/cart/items/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    // ---------------------------------------------------------
    // UPDATE ITEM (item not found → 404)
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        CartItemUpdateRequest request = new CartItemUpdateRequest(5);

        Mockito.when(cartService.updateItem(mockUser, 999L, 5))
                .thenThrow(new CartItemNotFoundException(999L));

        mockMvc.perform(put("/cart/items/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item with id 999 not found"));
    }

    // ---------------------------------------------------------
    // UPDATE ITEM (forbidden → 403)
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldReturnForbidden_whenItemBelongsToAnotherUser() throws Exception {
        CartItemUpdateRequest request = new CartItemUpdateRequest(5);

        Mockito.when(cartService.updateItem(mockUser, 10L, 5))
                .thenThrow(new ForbiddenException("You do not own this cart item"));

        mockMvc.perform(put("/cart/items/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You do not own this cart item"));
    }

    // ---------------------------------------------------------
    // REMOVE ITEM
    // ---------------------------------------------------------
    @Test
    void removeItem_shouldReturnUpdatedCart() throws Exception {
        Cart cart = new Cart(mockUser);
        cart.setId(1L);

        Mockito.when(cartService.removeItem(mockUser, 10L)).thenReturn(cart);

        CartResponse response = new CartResponse(1L, 1L, List.of(), 0);
        Mockito.when(cartMapper.toResponse(cart)).thenReturn(response);

        mockMvc.perform(delete("/cart/items/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ---------------------------------------------------------
    // REMOVE ITEM (item not found → 404)
    // ---------------------------------------------------------
    @Test
    void removeItem_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        Mockito.when(cartService.removeItem(mockUser, 999L))
                .thenThrow(new CartItemNotFoundException(999L));

        mockMvc.perform(delete("/cart/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item with id 999 not found"));
    }

    // ---------------------------------------------------------
    // CLEAR CART
    // ---------------------------------------------------------
    @Test
    void clearCart_shouldReturnEmptyCart() throws Exception {
        Cart cart = new Cart(mockUser);
        cart.setId(1L);

        Mockito.when(cartService.clearCart(mockUser)).thenReturn(cart);

        CartResponse response = new CartResponse(1L, 1L, List.of(), 0);
        Mockito.when(cartMapper.toResponse(cart)).thenReturn(response);

        mockMvc.perform(delete("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ---------------------------------------------------------
    // CLEAR CART (cart not found → 404)
    // ---------------------------------------------------------
    @Test
    void clearCart_shouldReturnNotFound_whenCartDoesNotExist() throws Exception {
        Mockito.when(cartService.clearCart(mockUser))
                .thenThrow(new CartNotFoundException(mockUser.getId()));

        mockMvc.perform(delete("/cart"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Cart with id 1 not found"));
    }

    // ---------------------------------------------------------
    // GET CART
    // ---------------------------------------------------------
    @Test
    void getCart_shouldReturnCart() throws Exception {
        Cart cart = new Cart(mockUser);
        cart.setId(1L);

        Mockito.when(cartService.getCart(mockUser)).thenReturn(cart);

        CartResponse response = new CartResponse(1L, 1L, List.of(), 0);
        Mockito.when(cartMapper.toResponse(cart)).thenReturn(response);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ---------------------------------------------------------
    // GET CART (cart not found → 404)
    // ---------------------------------------------------------
    @Test
    void getCart_shouldReturnNotFound_whenCartDoesNotExist() throws Exception {
        Mockito.when(cartService.getCart(mockUser))
                .thenThrow(new CartNotFoundException(mockUser.getId()));

        mockMvc.perform(get("/cart"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Cart with id 1 not found"));
    }
}