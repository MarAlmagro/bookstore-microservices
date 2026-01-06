package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Data Transfer Object for Book entity.
 * Used for transferring book information between services and clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {

    /**
     * Unique identifier for the book.
     */
    private Long id;

    /**
     * International Standard Book Number (ISBN).
     * Must be unique and between 10-13 characters.
     */
    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 13, message = "ISBN must be between 10 and 13 characters")
    private String isbn;

    /**
     * Title of the book.
     */
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /**
     * Author of the book.
     */
    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    /**
     * Detailed description of the book.
     */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Price of the book.
     * Must be greater than 0.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    /**
     * Current stock quantity available.
     * Must be 0 or greater.
     */
    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    /**
     * Category or genre of the book.
     */
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;
}
