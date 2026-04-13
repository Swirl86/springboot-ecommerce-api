package com.swirl.ecomengine.orderhistory.controller;

import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderRepository;
import com.swirl.ecomengine.order.exception.OrderNotFoundException;
import com.swirl.ecomengine.orderhistory.OrderHistoryEntry;
import com.swirl.ecomengine.orderhistory.OrderHistoryMapper;
import com.swirl.ecomengine.orderhistory.dto.OrderHistoryResponse;
import com.swirl.ecomengine.orderhistory.exception.OrderHistoryAccessDeniedException;
import com.swirl.ecomengine.orderhistory.service.OrderHistoryService;
import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order History", description = "View historical status changes for an order")
@RestController
@RequestMapping("/orders")
@Validated
public class OrderHistoryController {

    private final OrderHistoryService historyService;
    private final OrderRepository orderRepository;
    private final OrderHistoryMapper mapper;

    public OrderHistoryController(
            OrderHistoryService historyService,
            OrderRepository orderRepository,
            OrderHistoryMapper mapper
    ) {
        this.historyService = historyService;
        this.orderRepository = orderRepository;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------
    // GET ORDER HISTORY (PAGINATED)
    // ---------------------------------------------------------
    @Operation(
            summary = "Get order history (paginated)",
            description = "Returns a paginated and chronological list of all status changes for the specified order. "
                    + "Admins may view any order; users may only view their own."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order history retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "User is not allowed to view this order history"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{orderId}/history")
    public ResponseEntity<Page<OrderHistoryResponse>> getOrderHistory(
            @AuthenticatedUser User user,
            @PathVariable @Positive(message = "Order ID must be positive") Long orderId,
            @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // USER may only view their own order history
        if (!user.isAdmin() && !order.getUser().getId().equals(user.getId())) {
            throw new OrderHistoryAccessDeniedException(orderId);
        }

        Page<OrderHistoryEntry> historyPage = historyService.getHistory(orderId, pageable);

        Page<OrderHistoryResponse> responsePage = historyPage.map(mapper::toResponse);

        return ResponseEntity.ok(responsePage);
    }
}