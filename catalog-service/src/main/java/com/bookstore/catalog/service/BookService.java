package com.bookstore.catalog.service;

import com.bookstore.common.dto.BookDTO;

import java.util.List;

/**
 * Service interface for Book operations.
 * <p>
 * Defines business logic methods for managing books in the catalog.
 * All methods use DTOs for data transfer to maintain separation of concerns.
 * </p>
 */
public interface BookService {

    /**
     * Retrieve all books in the catalog
     *
     * @return list of all books as DTOs
     */
    List<BookDTO> findAll();

    /**
     * Retrieve a book by its unique identifier
     *
     * @param id the book ID
     * @return the book DTO
     * @throws com.bookstore.common.exception.ResourceNotFoundException if book not found
     */
    BookDTO findById(Long id);

    /**
     * Retrieve a book by its ISBN
     *
     * @param isbn the International Standard Book Number
     * @return the book DTO
     * @throws com.bookstore.common.exception.ResourceNotFoundException if book not found
     */
    BookDTO findByIsbn(String isbn);

    /**
     * Create a new book in the catalog
     *
     * @param bookDTO the book data to create
     * @return the created book DTO with generated ID
     * @throws com.bookstore.common.exception.InvalidRequestException if ISBN already exists
     */
    BookDTO create(BookDTO bookDTO);

    /**
     * Update an existing book
     *
     * @param id the book ID to update
     * @param bookDTO the updated book data
     * @return the updated book DTO
     * @throws com.bookstore.common.exception.ResourceNotFoundException if book not found
     * @throws com.bookstore.common.exception.InvalidRequestException if ISBN already exists for another book
     */
    BookDTO update(Long id, BookDTO bookDTO);

    /**
     * Delete a book from the catalog
     *
     * @param id the book ID to delete
     * @throws com.bookstore.common.exception.ResourceNotFoundException if book not found
     */
    void delete(Long id);

    /**
     * Find all books in a specific category
     *
     * @param category the book category/genre
     * @return list of books in the category
     */
    List<BookDTO> findByCategory(String category);

    /**
     * Find all books by author (case-insensitive partial match)
     *
     * @param author the author name or partial name
     * @return list of books by the author
     */
    List<BookDTO> findByAuthor(String author);

    /**
     * Search books by title or author
     *
     * @param searchTerm the search term to match against title or author
     * @return list of books matching the search criteria
     */
    List<BookDTO> search(String searchTerm);

    /**
     * Find all books with available stock (stock > 0)
     *
     * @return list of available books
     */
    List<BookDTO> findAvailableBooks();

    /**
     * Find books with low stock below the specified threshold
     *
     * @param threshold the stock threshold
     * @return list of books with low stock
     */
    List<BookDTO> findLowStockBooks(Integer threshold);

    /**
     * Update the stock quantity for a book
     *
     * @param id the book ID
     * @param quantity the quantity to add (positive) or remove (negative)
     * @return the updated book DTO
     * @throws com.bookstore.common.exception.ResourceNotFoundException if book not found
     * @throws com.bookstore.common.exception.InvalidRequestException if resulting stock would be negative
     */
    BookDTO updateStock(Long id, Integer quantity);
}
