package com.swirl.ecomengine.wishlist.controller;

import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.wishlist.WishlistMapper;
import com.swirl.ecomengine.wishlist.dto.WishlistResponse;
import com.swirl.ecomengine.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Manage wishlist items for the authenticated user")
public class WishlistController {

    private final WishlistService wishlistService;
    private final WishlistMapper mapper;

    // ============================================================
    // GET /wishlist
    // ============================================================
    @GetMapping
    @Operation(
            summary = "Get wishlist",
            description = "Returns all wishlist items for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public List<WishlistResponse> getWishlist(@AuthenticatedUser User user) {
        return wishlistService.getWishlist(user)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ============================================================
    // POST /wishlist/{productId}
    // ============================================================
    @PostMapping("/{productId}")
    @Operation(
            summary = "Add product to wishlist",
            description = "Adds a product to the authenticated user's wishlist"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product added to wishlist"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Product already in wishlist")
    })
    public ResponseEntity<Void> add(@PathVariable Long productId,
                                    @AuthenticatedUser User user) {
        wishlistService.addToWishlist(productId, user);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // DELETE /wishlist/{productId}
    // ============================================================
    @DeleteMapping("/{productId}")
    @Operation(
            summary = "Remove product from wishlist",
            description = "Removes a product from the authenticated user's wishlist"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product removed from wishlist"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found in wishlist")
    })
    public ResponseEntity<Void> remove(@PathVariable Long productId,
                                       @AuthenticatedUser User user) {
        wishlistService.removeFromWishlist(productId, user);
        return ResponseEntity.noContent().build();
    }

    // ============================================================
    // DELETE /wishlist
    // ============================================================
    @DeleteMapping
    @Operation(
            summary = "Clear wishlist",
            description = "Removes all items from the authenticated user's wishlist"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Wishlist cleared"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public ResponseEntity<Void> clear(@AuthenticatedUser User user) {
        wishlistService.clearWishlist(user);
        return ResponseEntity.noContent().build();
    }
}