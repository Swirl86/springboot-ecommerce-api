package com.swirl.ecomengine.order;

import java.util.List;

/**
 *  Represents the full lifecycle of an order in the system.
 *  <pre>
 * Full checkout → delivery → return/refund flow:
 *  - PENDING: Order created but not yet processed
 *  - PROCESSING: Order is being packed/handled
 *  - SHIPPED: Order has left the warehouse
 *  - COMPLETED: Order delivered or finalized
 *  - CANCELLED: Order cancelled by user or admin
 *  <pre>
 * TODO Future return/refund workflow:
 *  - RETURN_REQUESTED: Customer initiated a return
 *  - RETURNED: Returned items received and processed
 *  - REFUNDED: Refund issued to the customer
 *  <pre>
 * Each status defines which transitions are allowed.
 * Terminal states (CANCELLED, REFUNDED) do not allow further transitions.
 */
public enum OrderStatus {

    PENDING {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return to == PROCESSING || to == CANCELLED;
        }
    },

    PROCESSING {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return to == SHIPPED || to == CANCELLED;
        }
    },

    SHIPPED {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return to == COMPLETED;
        }
    },

    COMPLETED {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return to == RETURN_REQUESTED;
        }
    },

    CANCELLED {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return false; // terminal state
        }
    },

    RETURN_REQUESTED {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return to == RETURNED;
        }
    },

    RETURNED {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return to == REFUNDED;
        }
    },

    REFUNDED {
        @Override
        public boolean canTransitionTo(OrderStatus to) {
            return false; // terminal state
        }
    };

    /**
     * Default behavior: no transitions allowed.
     * Each state overrides this if transitions are permitted.
     */
    public boolean canTransitionTo(OrderStatus to) {
        return false;
    }

    public static final List<OrderStatus> VALID_STATE_FOR_REVIEW = List.of(
            COMPLETED,
            RETURNED,
            REFUNDED
    );

    public static final List<OrderStatus> ACTIVE_STATES = List.of(
            PENDING,
            PROCESSING,
            SHIPPED,
            RETURN_REQUESTED
    );

    public static final List<OrderStatus> HISTORICAL_STATES = List.of(
            COMPLETED,
            CANCELLED,
            RETURNED,
            REFUNDED
    );
}