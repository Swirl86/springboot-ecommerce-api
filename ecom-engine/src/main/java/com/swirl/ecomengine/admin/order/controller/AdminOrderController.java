package com.swirl.ecomengine.admin.order.controller;

import com.swirl.ecomengine.admin.order.service.AdminOrderService;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderMapper;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.order.dto.OrderResponse;
import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/orders")
@Tag(name = "Admin Orders", description = "Admin-only order management")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminOrderController {

    private final AdminOrderService service;
    private final OrderMapper mapper;

    public AdminOrderController(AdminOrderService service, OrderMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------
    // GET ALL ORDERS
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    @Operation(summary = "List all orders", description = "Returns all orders across all users. Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders returned successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied — admin only")
    })
    public ResponseEntity<List<OrderResponse>> getAllOrders(@AuthenticatedUser User admin) {
        List<Order> orders = service.getAllOrders(admin);

        return ResponseEntity.ok(
                orders.stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    // ---------------------------------------------------------
    // FILTER BY STATUS
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status/{status}")
    @Operation(summary = "Filter orders by status", description = "Returns all orders with the given status. Admin only.")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(
            @AuthenticatedUser User admin,
            @PathVariable OrderStatus status
    ) {
        List<Order> orders = service.getOrdersByStatus(status, admin);

        return ResponseEntity.ok(
                orders.stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    // ---------------------------------------------------------
    // GET ARCHIVED ORDERS (Soft-deleted)
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived")
    @Operation(summary = "List archived orders", description = "Returns all soft-deleted orders. Admin only.")
    public ResponseEntity<List<OrderResponse>> getArchivedOrders(@AuthenticatedUser User admin) {
        List<Order> orders = service.getDeletedOrders(admin);

        return ResponseEntity.ok(
                orders.stream()
                        .map(mapper::toResponse)
                        .toList()
        );
    }

    // ---------------------------------------------------------
    // RESTORE ORDER
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/restore")
    @Operation(summary = "Restore archived order", description = "Restores a soft-deleted order. Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order restored successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "403", description = "Access denied — admin only")
    })
    public ResponseEntity<Void> restoreOrder(
            @AuthenticatedUser User admin,
            @PathVariable Long id
    ) {
        service.restore(id, admin);
        return ResponseEntity.noContent().build();
    }

    // ---------------------------------------------------------
    // UPDATE ORDER STATUS
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Allows admin to update the status of any order.")
    public ResponseEntity<OrderResponse> updateStatus(
            @AuthenticatedUser User admin,
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @RequestParam(required = false) String reason
    ) {
        Order updated = service.updateStatus(id, status, admin, reason);
        return ResponseEntity.ok(mapper.toResponse(updated));
    }
}