package com.swirl.ecomengine.cart.controller;

import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.CartMapper;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.cart.dto.CartItemRequest;
import com.swirl.ecomengine.cart.dto.CartItemUpdateRequest;
import com.swirl.ecomengine.cart.dto.CartResponse;
import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cart", description = "Operations related to the user's shopping cart")
@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final CartMapper mapper;

    public CartController(CartService cartService, CartMapper mapper) {
        this.cartService = cartService;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------
    // GET CART
    // ---------------------------------------------------------
    @Operation(summary = "Get the user's cart", description = "Returns the authenticated user's active shopping cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticatedUser User user) {
        return ResponseEntity.ok(mapper.toResponse(cartService.getCart(user)));
    }

    // ---------------------------------------------------------
    // ADD ITEM
    // ---------------------------------------------------------
    @Operation(summary = "Add item to cart", description = "Adds a product to the user's cart or increases quantity if it already exists.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticatedUser User user,
            @Valid @RequestBody CartItemRequest request
    ) {
        Cart cart = cartService.addItem(user, request.productId(), request.quantity());
        return ResponseEntity.ok(mapper.toResponse(cart));
    }

    // ---------------------------------------------------------
    // UPDATE ITEM
    // ---------------------------------------------------------
    @Operation(summary = "Update cart item quantity", description = "Updates the quantity of an existing cart item.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item updated successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticatedUser User user,
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        Cart cart = cartService.updateItem(user, itemId, request.quantity());
        return ResponseEntity.ok(mapper.toResponse(cart));
    }

    // ---------------------------------------------------------
    // REMOVE ITEM
    // ---------------------------------------------------------
    @Operation(summary = "Remove item from cart", description = "Removes a specific item from the user's cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticatedUser User user,
            @PathVariable Long itemId
    ) {
        Cart cart = cartService.removeItem(user, itemId);
        return ResponseEntity.ok(mapper.toResponse(cart));
    }

    // ---------------------------------------------------------
    // CLEAR CART
    // ---------------------------------------------------------
    @Operation(summary = "Clear the cart", description = "Removes all items from the user's cart.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    })
    @DeleteMapping
    public ResponseEntity<CartResponse> clearCart(@AuthenticatedUser User user) {
        Cart cart = cartService.clearCart(user);
        return ResponseEntity.ok(mapper.toResponse(cart));
    }
}
