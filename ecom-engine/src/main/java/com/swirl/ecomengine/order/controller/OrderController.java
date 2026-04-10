package com.swirl.ecomengine.order.controller;

import com.swirl.ecomengine.order.OrderMapper;
import com.swirl.ecomengine.order.dto.OrderResponse;
import com.swirl.ecomengine.order.dto.UpdateOrderStatusRequest;
import com.swirl.ecomengine.order.service.OrderService;
import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Checkout and order management")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class OrderController {

    private final OrderService service;
    private final OrderMapper mapper;

    public OrderController(OrderService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------
    // CHECKOUT
    // ---------------------------------------------------------

    @PostMapping("/checkout")
    @Operation(
            summary = "Place an order",
            description = "Creates a new order from the authenticated user's cart and clears the cart after successful checkout."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Cart is empty or invalid request"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public OrderResponse checkout(@AuthenticatedUser User user) {
        return mapper.toResponse(service.placeOrder(user));
    }

    // ---------------------------------------------------------
    // ORDER HISTORY
    // ---------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "Get order history",
            description = "Returns all past orders for the authenticated user, sorted by newest first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public List<OrderResponse> getOrderHistory(@AuthenticatedUser User user) {
        return service.getOrderHistory(user).stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // ACTIVE ORDERS
    // ---------------------------------------------------------

    @GetMapping("/active")
    @Operation(
            summary = "Get active orders",
            description = "Returns all active orders (PENDING or PROCESSING) for the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active orders retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public List<OrderResponse> getActiveOrders(@AuthenticatedUser User user) {
        return service.getActiveOrders(user).stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET ORDER BY ID
    // ---------------------------------------------------------

    @GetMapping("/{id}")
    @Operation(
            summary = "Get order by ID",
            description = "Returns a specific order if it belongs to the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User does not have access to this order"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public OrderResponse getOrderById(
            @AuthenticatedUser User user,
            @PathVariable("id") Long id
    ) {
        return mapper.toResponse(service.getOrderById(user, id));
    }

    // ---------------------------------------------------------
    // UPDATE ORDER STATUS (ADMIN ONLY)
    // ---------------------------------------------------------

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update order status",
            description = "Allows ADMIN to update the status of an order. Users cannot update order status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "403", description = "User is not ADMIN"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public OrderResponse updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        return mapper.toResponse(service.updateStatus(id, request.status()));
    }
}