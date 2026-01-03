package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Data Transfer Object for individual items in an order.
 * Represents a single book item with quantity and price in an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    /**
     * Identifier of the book being ordered.
     */
    @NotNull(message = "Book ID is required")
    private Long bookId;

    /**
     * Quantity of books ordered.
     * Must be at least 1.
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    /**
     * Price per unit at the time of order.
     * Captured to preserve historical pricing.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    /**
     * Title of the book (captured at order time).
     */
    private String bookTitle;

    /**
     * ISBN of the book (captured at order time).
     */
    private String bookIsbn;
}
