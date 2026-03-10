package com.bookstore.catalog.repository;

import com.bookstore.catalog.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Book entity operations.
 * <p>
 * Extends JpaRepository to provide CRUD operations and custom query methods
 * for searching and filtering books in the catalog.
 * </p>
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find a book by its ISBN
     *
     * @param isbn the International Standard Book Number
     * @return Optional containing the book if found
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Find all books in a specific category
     *
     * @param category the book category/genre
     * @return list of books in the category
     */
    List<Book> findByCategory(String category);

    /**
     * Find all books by a specific author (case-insensitive partial match)
     *
     * @param author the author name or partial name
     * @return list of books by the author
     * @deprecated Use {@link #findByAuthorContainingIgnoreCase(String, Pageable)} instead
     */
    @Deprecated
    List<Book> findByAuthorContainingIgnoreCase(String author);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    /**
     * Find all books with title containing the search term (case-insensitive)
     *
     * @param title the title search term
     * @return list of books matching the title
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Find all books with stock greater than zero (available books)
     *
     * @return list of books in stock
     * @deprecated Use {@link #findByStockGreaterThan(Integer, Pageable)} instead
     */
    @Deprecated
    List<Book> findByStockGreaterThan(Integer stock);

    Page<Book> findByStockGreaterThan(Integer stock, Pageable pageable);

    /**
     * Check if a book with the given ISBN already exists
     *
     * @param isbn the ISBN to check
     * @return true if book exists, false otherwise
     */
    boolean existsByIsbn(String isbn);

    /**
     * Find books by category with stock availability
     *
     * @param category the book category
     * @param minStock minimum stock threshold
     * @return list of available books in the category
     */
    @Query("SELECT b FROM Book b WHERE b.category = :category AND b.stock > :minStock")
    List<Book> findByCategoryAndStockGreaterThan(
        @Param("category") String category,
        @Param("minStock") Integer minStock
    );

    /**
     * Search books by title or author (case-insensitive)
     *
     * @param searchTerm the search term to match against title or author
     * @return list of books matching the search criteria
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchByTitleOrAuthor(@Param("searchTerm") String searchTerm);

    /**
     * Find all books ordered by creation date (newest first)
     *
     * @return list of books ordered by creation date descending
     */
    List<Book> findAllByOrderByCreatedAtDesc();

    /**
     * Find books with low stock (below threshold)
     *
     * @param threshold the stock threshold
     * @return list of books with stock below threshold
     */
    @Query("SELECT b FROM Book b WHERE b.stock < :threshold AND b.stock > 0")
    List<Book> findLowStockBooks(@Param("threshold") Integer threshold);

    Page<Book> findByCategory(String category, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Book> searchByTitleOrAuthor(@Param("searchTerm") String searchTerm, Pageable pageable);
}
