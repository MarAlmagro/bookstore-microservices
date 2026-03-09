package com.bookstore.catalog.fixtures;

import com.bookstore.catalog.entity.Book;
import com.bookstore.common.dto.BookDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Test fixtures for creating test data for Book entities and Dtos.
 * Provides static factory methods for consistent test data across test classes.
 */
public class BookTestFixtures {

    /**
     * Creates a sample Book entity with default values
     *
     * @return a Book entity
     */
    public static Book createSampleBook() {
        return Book.builder()
                .id(1L)
                .isbn("9780134685")  // Valid 10-character ISBN
                .title("Effective Java")
                .author("Joshua Bloch")
                .description("Best practices for the Java platform")
                .price(new BigDecimal("45.99"))
                .stock(100)
                .category("Programming")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a Book entity with specified ID
     *
     * @param id the book ID
     * @return a Book entity
     */
    public static Book createBookWithId(Long id) {
        Book book = createSampleBook();
        book.setId(id);
        return book;
    }

    /**
     * Creates a Book entity with specified ISBN
     *
     * @param isbn the ISBN
     * @return a Book entity
     */
    public static Book createBookWithIsbn(String isbn) {
        Book book = createSampleBook();
        book.setIsbn(isbn);
        return book;
    }

    /**
     * Creates a Book entity with specified stock
     *
     * @param stock the stock quantity
     * @return a Book entity
     */
    public static Book createBookWithStock(Integer stock) {
        Book book = createSampleBook();
        book.setStock(stock);
        return book;
    }

    /**
     * Creates a Book entity with specified category
     *
     * @param category the category
     * @return a Book entity
     */
    public static Book createBookWithCategory(String category) {
        Book book = createSampleBook();
        book.setCategory(category);
        return book;
    }

    /**
     * Creates a Book entity with specified author
     *
     * @param author the author name
     * @return a Book entity
     */
    public static Book createBookWithAuthor(String author) {
        Book book = createSampleBook();
        book.setAuthor(author);
        return book;
    }

    /**
     * Creates a second sample Book entity for testing multiple books
     *
     * @return a Book entity
     */
    public static Book createSecondSampleBook() {
        return Book.builder()
                .id(2L)
                .isbn("9780132350")  // Valid 10-character ISBN
                .title("Clean Code")
                .author("Robert C. Martin")
                .description("A handbook of agile software craftsmanship")
                .price(new BigDecimal("42.50"))
                .stock(75)
                .category("Programming")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a third sample Book entity with low stock
     *
     * @return a Book entity
     */
    public static Book createLowStockBook() {
        return Book.builder()
                .id(3L)
                .isbn("9780201616")  // Valid 10-character ISBN
                .title("The Pragmatic Programmer")
                .author("Andrew Hunt")
                .description("Your journey to mastery")
                .price(new BigDecimal("38.99"))
                .stock(5)
                .category("Programming")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a Book entity with zero stock
     *
     * @return a Book entity
     */
    public static Book createOutOfStockBook() {
        return Book.builder()
                .id(4L)
                .isbn("9780321125")  // Valid 10-character ISBN
                .title("Domain-Driven Design")
                .author("Eric Evans")
                .description("Tackling complexity in the heart of software")
                .price(new BigDecimal("55.00"))
                .stock(0)
                .category("Software Architecture")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a sample BookDto with default values
     *
     * @return a BookDto
     */
    public static BookDto createSampleBookDto() {
        return BookDto.builder()
                .id(1L)
                .isbn("9780134685")  // Valid 10-character ISBN
                .title("Effective Java")
                .author("Joshua Bloch")
                .description("Best practices for the Java platform")
                .price(new BigDecimal("45.99"))
                .stock(100)
                .category("Programming")
                .build();
    }

    /**
     * Creates a BookDto for creating a new book (no ID)
     *
     * @return a BookDto
     */
    public static BookDto createNewBookDto() {
        return BookDto.builder()
                .isbn("9781234567")  // Valid 10-character ISBN
                .title("New Test Book")
                .author("Test Author")
                .description("A test book description")
                .price(new BigDecimal("29.99"))
                .stock(50)
                .category("Testing")
                .build();
    }

    /**
     * Creates a BookDto with invalid data (for validation testing)
     *
     * @return a BookDto with invalid data
     */
    public static BookDto createInvalidBookDto() {
        return BookDto.builder()
                .isbn("")  // Invalid: empty ISBN
                .title("")  // Invalid: empty title
                .author("")  // Invalid: empty author
                .price(new BigDecimal("-10.00"))  // Invalid: negative price
                .stock(-5)  // Invalid: negative stock
                .build();
    }

    /**
     * Creates a BookDto for update operations
     *
     * @return a BookDto
     */
    public static BookDto createUpdateBookDto() {
        return BookDto.builder()
                .id(1L)
                .isbn("9780134685")  // Valid 10-character ISBN
                .title("Effective Java - Updated Edition")
                .author("Joshua Bloch")
                .description("Updated description with new content")
                .price(new BigDecimal("49.99"))
                .stock(120)
                .category("Programming")
                .build();
    }
}
