package com.swirl.ecomengine.order.dto;

import com.swirl.ecomengine.order.OrderStatus;

/**
 * Request body for updating order status.
 * Used by ADMIN in PATCH /orders/{id}/status.
 */
public record UpdateOrderStatusRequest(OrderStatus status) {}
