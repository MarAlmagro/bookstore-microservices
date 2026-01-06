package com.bookstore.catalog.controller;

import com.bookstore.catalog.service.BookService;
import com.bookstore.common.dto.BookDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * REST Controller for managing books in the catalog.
 * <p>
 * Provides endpoints for CRUD operations and search functionality.
 * All endpoints are prefixed with /api/v1/books.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/books")
@Validated
@Tag(name = "Book Management", description = "APIs for managing books in the catalog")
public class BookController {

    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Get all books in the catalog
     *
     * @return list of all books
     */
    @GetMapping
    @Operation(summary = "Get all books", description = "Retrieves all books from the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all books",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class)))
    })
    public ResponseEntity<List<BookDto>> getAllBooks() {
        logger.info("GET /api/v1/books - Fetching all books");
        List<BookDto> books = bookService.findAll();
        logger.info("Retrieved {} books", books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Get a book by ID
     *
     * @param id the book ID
     * @return the book details
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Retrieves a specific book by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    public ResponseEntity<BookDto> getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true)
            @PathVariable @Min(1) Long id) {
        logger.info("GET /api/v1/books/{} - Fetching book by ID", id);
        BookDto book = bookService.findById(id);
        return ResponseEntity.ok(book);
    }

    /**
     * Get a book by ISBN
     *
     * @param isbn the International Standard Book Number
     * @return the book details
     */
    @GetMapping("/isbn/{isbn}")
    @Operation(summary = "Get book by ISBN", description = "Retrieves a book by its ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    public ResponseEntity<BookDto> getBookByIsbn(
            @Parameter(description = "ISBN of the book to retrieve", required = true)
            @PathVariable @NotBlank String isbn) {
        logger.info("GET /api/v1/books/isbn/{} - Fetching book by ISBN", isbn);
        BookDto book = bookService.findByIsbn(isbn);
        return ResponseEntity.ok(book);
    }

    /**
     * Create a new book
     *
     * @param bookDto the book data to create
     * @return the created book with generated ID
     */
    @PostMapping
    @Operation(summary = "Create a new book", description = "Adds a new book to the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or ISBN already exists", content = @Content)
    })
    public ResponseEntity<BookDto> createBook(
            @Parameter(description = "Book details to create", required = true)
            @Valid @RequestBody BookDto bookDto) {
        logger.info("POST /api/v1/books - Creating new book with ISBN: {}", bookDto.getIsbn());
        BookDto createdBook = bookService.create(bookDto);
        logger.info("Created book with ID: {}", createdBook.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    /**
     * Update an existing book
     *
     * @param id the book ID to update
     * @param bookDto the updated book data
     * @return the updated book
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a book", description = "Updates an existing book's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    public ResponseEntity<BookDto> updateBook(
            @Parameter(description = "ID of the book to update", required = true)
            @PathVariable @Min(1) Long id,
            @Parameter(description = "Updated book details", required = true)
            @Valid @RequestBody BookDto bookDto) {
        logger.info("PUT /api/v1/books/{} - Updating book", id);
        BookDto updatedBook = bookService.update(id, bookDto);
        logger.info("Updated book with ID: {}", updatedBook.getId());
        return ResponseEntity.ok(updatedBook);
    }

    /**
     * Delete a book
     *
     * @param id the book ID to delete
     * @return no content response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a book", description = "Removes a book from the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Book deleted successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "ID of the book to delete", required = true)
            @PathVariable @Min(1) Long id) {
        logger.info("DELETE /api/v1/books/{} - Deleting book", id);
        bookService.delete(id);
        logger.info("Deleted book with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search books by title or author
     *
     * @param searchTerm the search term
     * @return list of books matching the search criteria
     */
    @GetMapping("/search")
    @Operation(summary = "Search books", description = "Searches books by title or author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class)))
    })
    public ResponseEntity<List<BookDto>> searchBooks(
            @Parameter(description = "Search term to match against title or author", required = true)
            @RequestParam @NotBlank String searchTerm) {
        logger.info("GET /api/v1/books/search?searchTerm={} - Searching books", searchTerm);
        List<BookDto> books = bookService.search(searchTerm);
        logger.info("Found {} books matching search term", books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Get books by category
     *
     * @param category the book category/genre
     * @return list of books in the category
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Get books by category", description = "Retrieves all books in a specific category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class)))
    })
    public ResponseEntity<List<BookDto>> getBooksByCategory(
            @Parameter(description = "Category/genre to filter by", required = true)
            @PathVariable @NotBlank String category) {
        logger.info("GET /api/v1/books/category/{} - Fetching books by category", category);
        List<BookDto> books = bookService.findByCategory(category);
        logger.info("Found {} books in category: {}", books.size(), category);
        return ResponseEntity.ok(books);
    }

    /**
     * Get books by author
     *
     * @param author the author name (partial match supported)
     * @return list of books by the author
     */
    @GetMapping("/author/{author}")
    @Operation(summary = "Get books by author", description = "Retrieves all books by a specific author (case-insensitive partial match)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class)))
    })
    public ResponseEntity<List<BookDto>> getBooksByAuthor(
            @Parameter(description = "Author name to search for", required = true)
            @PathVariable @NotBlank String author) {
        logger.info("GET /api/v1/books/author/{} - Fetching books by author", author);
        List<BookDto> books = bookService.findByAuthor(author);
        logger.info("Found {} books by author: {}", books.size(), author);
        return ResponseEntity.ok(books);
    }

    /**
     * Get all available books (in stock)
     *
     * @return list of available books
     */
    @GetMapping("/available")
    @Operation(summary = "Get available books", description = "Retrieves all books that are currently in stock")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved available books",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class)))
    })
    public ResponseEntity<List<BookDto>> getAvailableBooks() {
        logger.info("GET /api/v1/books/available - Fetching available books");
        List<BookDto> books = bookService.findAvailableBooks();
        logger.info("Found {} available books", books.size());
        return ResponseEntity.ok(books);
    }

    /**
     * Get books with low stock
     *
     * @param threshold the stock threshold (default: 10)
     * @return list of books with low stock
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock books", description = "Retrieves books with stock below the specified threshold")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved low stock books",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class)))
    })
    public ResponseEntity<List<BookDto>> getLowStockBooks(
            @Parameter(description = "Stock threshold (default: 10)")
            @RequestParam(defaultValue = "10") @Min(1) Integer threshold) {
        logger.info("GET /api/v1/books/low-stock?threshold={} - Fetching low stock books", threshold);
        List<BookDto> books = bookService.findLowStockBooks(threshold);
        logger.info("Found {} books with stock below {}", books.size(), threshold);
        return ResponseEntity.ok(books);
    }

    /**
     * Update book stock
     *
     * @param id the book ID
     * @param quantity the quantity to add (positive) or remove (negative)
     * @return the updated book
     */
    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update book stock", description = "Updates the stock quantity for a book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Stock updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid quantity or insufficient stock", content = @Content),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content)
    })
    public ResponseEntity<BookDto> updateStock(
            @Parameter(description = "ID of the book to update stock", required = true)
            @PathVariable @Min(1) Long id,
            @Parameter(description = "Quantity to add (positive) or remove (negative)", required = true)
            @RequestParam Integer quantity) {
        logger.info("PATCH /api/v1/books/{}/stock?quantity={} - Updating stock", id, quantity);
        BookDto updatedBook = bookService.updateStock(id, quantity);
        logger.info("Updated stock for book ID: {} to {}", id, updatedBook.getStock());
        return ResponseEntity.ok(updatedBook);
    }
}
