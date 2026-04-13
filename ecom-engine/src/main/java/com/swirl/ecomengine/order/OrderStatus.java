package com.swirl.ecomengine.order;

/**
 * Represents the lifecycle of an order in the system.
 * <pre>
 * Full checkout → delivery → return/refund flow:
 * <pre>
 *  - PENDING: Order created but not yet processed
 *  - PROCESSING: Order is being packed/handled
 *  - SHIPPED: Order has left the warehouse
 *  - COMPLETED: Order delivered or finalized
 *  - CANCELLED: Order cancelled by user or admin
 * <pre>
 * TODO Future return/refund workflow:
 *  - RETURN_REQUESTED: Customer initiated a return
 *  - RETURNED: Returned items received and processed
 *  - REFUNDED: Refund issued to the customer
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    COMPLETED,
    CANCELLED,

    // Future expansion
    RETURN_REQUESTED,
    RETURNED,
    REFUNDED
}
