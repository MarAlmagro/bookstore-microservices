package com.bookstore.catalog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Book entity representing a book in the catalog.
 * <p>
 * This entity is mapped to the 'books' table in MySQL database.
 * Includes audit fields for tracking creation and update timestamps.
 * </p>
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_isbn", columnList = "isbn", unique = true),
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_author", columnList = "author")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    /**
     * Unique identifier for the book
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * International Standard Book Number (ISBN)
     * Must be unique across all books
     */
    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 20, message = "ISBN must be between 10 and 20 characters")
    private String isbn;

    /**
     * Title of the book
     */
    @Column(name = "title", nullable = false, length = 255)
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    /**
     * Author of the book
     */
    @Column(name = "author", nullable = false, length = 255)
    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    private String author;

    /**
     * Detailed description of the book
     */
    @Column(name = "description", columnDefinition = "TEXT")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Price of the book in the system currency
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    /**
     * Current stock quantity available
     */
    @Column(name = "stock", nullable = false)
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    /**
     * Category or genre of the book
     */
    @Column(name = "category", length = 100)
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    /**
     * Timestamp when the book was created
     * Automatically set on entity creation
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the book was last updated
     * Automatically updated on entity modification
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle callback to set createdAt and updatedAt before persisting
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * JPA lifecycle callback to update updatedAt before updating
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
