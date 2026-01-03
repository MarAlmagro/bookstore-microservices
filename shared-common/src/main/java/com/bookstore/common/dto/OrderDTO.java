package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order.
 * Represents a customer order containing one or more items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    /**
     * Unique identifier for the order.
     */
    private String id;

    /**
     * Identifier of the user who placed the order.
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * List of items in the order.
     * Must contain at least one item.
     */
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemDTO> items;

    /**
     * Total amount for the entire order.
     * Auto-calculated by the service during order creation.
     */
    private BigDecimal totalAmount;

    /**
     * Current status of the order.
     * Valid values: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
     * Auto-set to PENDING during order creation.
     */
    private String status;

    /**
     * Timestamp when the order was created.
     */
    private LocalDateTime createdAt;
}
