package com.bookstore.catalog.service;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.mapper.BookMapper;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of BookService interface.
 * <p>
 * Provides business logic for managing books in the catalog.
 * Includes validation, error handling, and transaction management.
 * </p>
 */
@Service
@Transactional
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> findAll() {
        logger.debug("Fetching all books from catalog");
        List<Book> books = bookRepository.findAll();
        logger.debug("Found {} books in catalog", books.size());
        return bookMapper.toDTOList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDTO findById(Long id) {
        logger.debug("Fetching book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Book not found with id: {}", id);
                    return new ResourceNotFoundException("Book not found with id: " + id);
                });
        logger.debug("Found book: {}", book.getTitle());
        return bookMapper.toDTO(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDTO findByIsbn(String isbn) {
        logger.debug("Fetching book with ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> {
                    logger.error("Book not found with ISBN: {}", isbn);
                    return new ResourceNotFoundException("Book not found with ISBN: " + isbn);
                });
        logger.debug("Found book: {}", book.getTitle());
        return bookMapper.toDTO(book);
    }

    @Override
    public BookDTO create(BookDTO bookDTO) {
        logger.debug("Creating new book with ISBN: {}", bookDTO.getIsbn());

        // Check if book with same ISBN already exists
        if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
            logger.error("Book with ISBN {} already exists", bookDTO.getIsbn());
            throw new InvalidRequestException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
        }

        Book book = bookMapper.toEntity(bookDTO);
        Book savedBook = bookRepository.save(book);
        logger.info("Created new book with id: {} and title: {}", savedBook.getId(), savedBook.getTitle());
        return bookMapper.toDTO(savedBook);
    }

    @Override
    public BookDTO update(Long id, BookDTO bookDTO) {
        logger.debug("Updating book with id: {}", id);

        // Find existing book
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Book not found with id: {}", id);
                    return new ResourceNotFoundException("Book not found with id: " + id);
                });

        // Check if ISBN is being changed to one that already exists
        if (!existingBook.getIsbn().equals(bookDTO.getIsbn())) {
            if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
                logger.error("Cannot update book: ISBN {} already exists for another book", bookDTO.getIsbn());
                throw new InvalidRequestException("Book with ISBN " + bookDTO.getIsbn() + " already exists");
            }
        }

        // Update the existing book entity
        bookMapper.updateEntityFromDTO(bookDTO, existingBook);
        Book updatedBook = bookRepository.save(existingBook);
        logger.info("Updated book with id: {}", updatedBook.getId());
        return bookMapper.toDTO(updatedBook);
    }

    @Override
    public void delete(Long id) {
        logger.debug("Deleting book with id: {}", id);

        // Verify book exists before deleting
        if (!bookRepository.existsById(id)) {
            logger.error("Book not found with id: {}", id);
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }

        bookRepository.deleteById(id);
        logger.info("Deleted book with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> findByCategory(String category) {
        logger.debug("Fetching books in category: {}", category);
        List<Book> books = bookRepository.findByCategory(category);
        logger.debug("Found {} books in category: {}", books.size(), category);
        return bookMapper.toDTOList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> findByAuthor(String author) {
        logger.debug("Fetching books by author: {}", author);
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        logger.debug("Found {} books by author containing: {}", books.size(), author);
        return bookMapper.toDTOList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> search(String searchTerm) {
        logger.debug("Searching books with term: {}", searchTerm);
        List<Book> books = bookRepository.searchByTitleOrAuthor(searchTerm);
        logger.debug("Found {} books matching search term: {}", books.size(), searchTerm);
        return bookMapper.toDTOList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> findAvailableBooks() {
        logger.debug("Fetching all available books (stock > 0)");
        List<Book> books = bookRepository.findByStockGreaterThan(0);
        logger.debug("Found {} available books", books.size());
        return bookMapper.toDTOList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDTO> findLowStockBooks(Integer threshold) {
        logger.debug("Fetching books with stock below threshold: {}", threshold);
        List<Book> books = bookRepository.findLowStockBooks(threshold);
        logger.debug("Found {} books with low stock", books.size());
        return bookMapper.toDTOList(books);
    }

    @Override
    public BookDTO updateStock(Long id, Integer quantity) {
        logger.debug("Updating stock for book id: {} by quantity: {}", id, quantity);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Book not found with id: {}", id);
                    return new ResourceNotFoundException("Book not found with id: " + id);
                });

        int newStock = book.getStock() + quantity;

        // Validate that stock doesn't go negative
        if (newStock < 0) {
            logger.error("Cannot update stock: resulting stock would be negative. Current: {}, Change: {}",
                    book.getStock(), quantity);
            throw new InvalidRequestException(
                    "Insufficient stock. Available: " + book.getStock() + ", Requested: " + Math.abs(quantity));
        }

        book.setStock(newStock);
        Book updatedBook = bookRepository.save(book);
        logger.info("Updated stock for book id: {} to {}", id, newStock);
        return bookMapper.toDTO(updatedBook);
    }
}
