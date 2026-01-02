package com.bookstore.common.constants;

/**
 * Enum representing the various states of an order in the system.
 * <p>
 * Order lifecycle: PENDING → CONFIRMED → SHIPPED → DELIVERED
 * Orders can be CANCELLED at any stage before DELIVERED.
 * </p>
 */
public enum OrderStatus {
    /**
     * Order has been created but not yet confirmed
     */
    PENDING,

    /**
     * Order has been confirmed and is being processed
     */
    CONFIRMED,

    /**
     * Order has been shipped to the customer
     */
    SHIPPED,

    /**
     * Order has been delivered to the customer
     */
    DELIVERED,

    /**
     * Order has been cancelled
     */
    CANCELLED
}
