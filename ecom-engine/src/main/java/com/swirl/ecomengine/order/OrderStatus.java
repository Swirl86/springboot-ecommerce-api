package com.swirl.ecomengine.order;

/**
 * Represents the lifecycle of an order in the system.
 * <pre>
 * Current statuses cover the full checkout → delivery flow:
 *  - PENDING: Order created but not yet processed
 *  - PROCESSING: Order is being packed/handled
 *  - SHIPPED: Order has left the warehouse
 *  - COMPLETED: Order delivered or finalized
 *  - CANCELLED: Order cancelled by user or admin
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    COMPLETED,
    CANCELLED
}

/*
 * Todo
 * Future expansion ideas:
 *  - RETURN_REQUESTED: Customer has initiated a return
 *  - RETURNED: Returned items received and processed
 *  - REFUNDED: Refund issued to the customer
 *
 * These additional statuses would enable a full return/refund workflow
 * if implemented later.
 */
