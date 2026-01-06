package com.bookstore.catalog.controller;

import com.bookstore.common.dto.BookDto;
import com.bookstore.catalog.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for managing books in the catalog.
 */
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Books", description = "Catalog management APIs")
public class BookController {

        private final BookService bookService;

        @Operation(summary = "Get all books", description = "Retrieves all books currently in the catalog")
        @GetMapping
        public ResponseEntity<List<BookDto>> getAllBooks() {
                log.debug("REST request to get all books");
                return ResponseEntity.ok(bookService.findAll());
        }

        @Operation(summary = "Get book by ID", description = "Retrieves a single book by its unique ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Book found"),
                        @ApiResponse(responseCode = "404", description = "Book not found")
        })
        @GetMapping("/{id}")
        public ResponseEntity<BookDto> getBookById(@PathVariable Long id) {
                log.debug("REST request to get book : {}", id);
                return ResponseEntity.ok(bookService.findById(id));
        }

        @Operation(summary = "Get book by ISBN", description = "Retrieves a single book by its ISBN")
        @GetMapping("/isbn/{isbn}")
        public ResponseEntity<BookDto> getBookByIsbn(@PathVariable String isbn) {
                log.debug("REST request to get book by ISBN: {}", isbn);
                return ResponseEntity.ok(bookService.findByIsbn(isbn));
        }

        @Operation(summary = "Create a new book", description = "Adds a new book to the catalog")
        @PostMapping
        public ResponseEntity<BookDto> createBook(@Valid @RequestBody BookDto bookDto) {
                log.debug("REST request to save book : {}", bookDto);
                BookDto result = bookService.create(bookDto);
                return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }

        @Operation(summary = "Update an existing book", description = "Updates book details by ID")
        @PutMapping("/{id}")
        public ResponseEntity<BookDto> updateBook(@PathVariable Long id, @Valid @RequestBody BookDto bookDto) {
                log.debug("REST request to update book : {}, {}", id, bookDto);
                return ResponseEntity.ok(bookService.update(id, bookDto));
        }

        @Operation(summary = "Delete a book", description = "Removes a book from the catalog by ID")
        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
                log.debug("REST request to delete book : {}", id);
                bookService.delete(id);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Search books", description = "Searches books by title or author")
        @GetMapping("/search")
        public ResponseEntity<List<BookDto>> searchBooks(@RequestParam String query) {
                log.debug("REST request to search books for query: {}", query);
                return ResponseEntity.ok(bookService.search(query));
        }

        @Operation(summary = "Get books by category", description = "Retrieves all books in a specific category")
        @GetMapping("/category/{category}")
        public ResponseEntity<List<BookDto>> getBooksByCategory(@PathVariable String category) {
                log.debug("REST request to get books by category: {}", category);
                return ResponseEntity.ok(bookService.findByCategory(category));
        }

        @Operation(summary = "Update book stock", description = "Updates the stock level for a book")
        @PatchMapping("/{id}/stock")
        public ResponseEntity<BookDto> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
                log.debug("REST request to update stock for book : {} by {}", id, quantity);
                return ResponseEntity.ok(bookService.updateStock(id, quantity));
        }
}
