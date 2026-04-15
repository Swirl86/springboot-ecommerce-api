package com.swirl.ecomengine.order.controller;

import com.swirl.ecomengine.order.Order;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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
            description = "Returns a paginated list of past orders for the authenticated user, sorted by newest first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public Page<OrderResponse> getOrderHistory(
            @AuthenticatedUser User user,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return service.getOrderHistory(user, pageable)
                .map(mapper::toResponse);
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

    @Operation(
            summary = "Update order status",
            description = "Allows an admin to update the status of an order."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to update order status"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticatedUser User user,
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request
    ) {
        Order updated = service.updateStatus(id, request.status(), user, request.reason());
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    // ---------------------------------------------------------
    // DELETE ORDER
    // ---------------------------------------------------------
    @Operation(summary = "Delete an order", description = "Soft-deletes an order. Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @AuthenticatedUser User admin,
            @PathVariable Long id
    ) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // GET /orders/deleted — list soft-deleted orders (ADMIN)
    // ---------------------------------------------------------
    @Operation(
            summary = "List deleted orders",
            description = "Returns all soft-deleted orders. Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Deleted orders returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied — admin only")
    })
    @GetMapping("/deleted")
    public ResponseEntity<List<Order>> getDeletedOrders(
            @AuthenticatedUser User admin
    ) {
        return ResponseEntity.ok(service.getDeletedOrders(admin));
    }

    // ---------------------------------------------------------
    // RESTORE ORDER (ADMIN ONLY)
    // ---------------------------------------------------------
    @Operation(
            summary = "Restore a soft-deleted order",
            description = "Restores an order that was previously soft-deleted. Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order restored successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — admin only")
    })
    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreOrder(
            @AuthenticatedUser User admin,
            @PathVariable Long id
    ) {
        service.restore(id, admin);
        return ResponseEntity.noContent().build();
    }
}