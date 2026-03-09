package com.bookstore.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class OrderDto {

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
    @lombok.Setter(lombok.AccessLevel.NONE)
    @lombok.Getter(lombok.AccessLevel.NONE)
    private List<OrderItemDto> items;

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

    @Builder
    public OrderDto(String id, Long userId, @Singular List<OrderItemDto> items, BigDecimal totalAmount, String status,
            LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.items = items != null ? new ArrayList<>(items) : null;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void setItems(List<OrderItemDto> items) {
        this.items = items != null ? new ArrayList<>(items) : null;
    }

    public List<OrderItemDto> getItems() {
        return items != null ? new ArrayList<>(items) : null;
    }
}
