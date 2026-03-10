package com.bookstore.catalog.service;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.mapper.BookMapper;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.common.dto.BookDto;
import com.bookstore.common.dto.PageRequestDto;
import com.bookstore.common.dto.PageResponseDto;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.util.PageMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    private static final String BOOK_NOT_FOUND_LOG = "Book not found with id: {}";
    private static final String BOOK_NOT_FOUND_MSG = "Book not found with id: ";

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findAll() {
        logger.debug("Fetching all books from catalog");
        List<Book> books = bookRepository.findAll();
        logger.debug("Found {} books in catalog", books.size());
        return bookMapper.toDtoList(books);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "books", key = "#id")
    public BookDto findById(Long id) {
        logger.debug("Fetching book with id: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(BOOK_NOT_FOUND_LOG, id);
                    return new ResourceNotFoundException(BOOK_NOT_FOUND_MSG + id);
                });
        logger.debug("Found book: {}", book.getTitle());
        return bookMapper.toDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto findByIsbn(String isbn) {
        logger.debug("Fetching book with ISBN: {}", isbn);
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> {
                    logger.error("Book not found with ISBN: {}", isbn);
                    return new ResourceNotFoundException("Book not found with ISBN: " + isbn);
                });
        logger.debug("Found book: {}", book.getTitle());
        return bookMapper.toDto(book);
    }

    @Override
    @CacheEvict(value = {"books", "booksByCategory", "allBooks"}, allEntries = true)
    public BookDto create(BookDto bookDto) {
        logger.debug("Creating new book with ISBN: {}", bookDto.getIsbn());

        // Check if book with same ISBN already exists
        if (bookRepository.existsByIsbn(bookDto.getIsbn())) {
            logger.error("Book with ISBN {} already exists", bookDto.getIsbn());
            throw new InvalidRequestException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }

        Book book = bookMapper.toEntity(bookDto);
        Book savedBook = bookRepository.save(book);
        logger.info("Created new book with id: {} and title: {}", savedBook.getId(), savedBook.getTitle());
        return bookMapper.toDto(savedBook);
    }

    @Override
    @CachePut(value = "books", key = "#id")
    @CacheEvict(value = {"booksByCategory", "allBooks"}, allEntries = true)
    public BookDto update(Long id, BookDto bookDto) {
        logger.debug("Updating book with id: {}", id);

        // Find existing book
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(BOOK_NOT_FOUND_LOG, id);
                    return new ResourceNotFoundException(BOOK_NOT_FOUND_MSG + id);
                });

        // Check if ISBN is being changed to one that already exists
        if (!existingBook.getIsbn().equals(bookDto.getIsbn()) && bookRepository.existsByIsbn(bookDto.getIsbn())) {
            logger.error("Cannot update book: ISBN {} already exists for another book", bookDto.getIsbn());
            throw new InvalidRequestException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }

        // Update the existing book entity
        bookMapper.updateEntityFromDto(bookDto, existingBook);
        Book updatedBook = bookRepository.save(existingBook);
        logger.info("Updated book with id: {}", updatedBook.getId());
        return bookMapper.toDto(updatedBook);
    }

    @Override
    @CacheEvict(value = {"books", "booksByCategory", "allBooks"}, allEntries = true)
    public void delete(Long id) {
        logger.debug("Deleting book with id: {}", id);

        // Verify book exists before deleting
        if (!bookRepository.existsById(id)) {
            logger.error(BOOK_NOT_FOUND_LOG, id);
            throw new ResourceNotFoundException(BOOK_NOT_FOUND_MSG + id);
        }

        bookRepository.deleteById(id);
        logger.info("Deleted book with id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "booksByCategory", key = "#category")
    public List<BookDto> findByCategory(String category) {
        logger.debug("Fetching books in category: {}", category);
        List<Book> books = bookRepository.findByCategory(category);
        logger.debug("Found {} books in category: {}", books.size(), category);
        return bookMapper.toDtoList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findByAuthor(String author) {
        logger.debug("Fetching books by author: {}", author);
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        logger.debug("Found {} books by author containing: {}", books.size(), author);
        return bookMapper.toDtoList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> search(String searchTerm) {
        logger.debug("Searching books with term: {}", searchTerm);
        List<Book> books = bookRepository.searchByTitleOrAuthor(searchTerm);
        logger.debug("Found {} books matching search term: {}", books.size(), searchTerm);
        return bookMapper.toDtoList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findAvailableBooks() {
        logger.debug("Fetching all available books (stock > 0)");
        List<Book> books = bookRepository.findByStockGreaterThan(0);
        logger.debug("Found {} available books", books.size());
        return bookMapper.toDtoList(books);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findLowStockBooks(Integer threshold) {
        logger.debug("Fetching books with stock below threshold: {}", threshold);
        List<Book> books = bookRepository.findLowStockBooks(threshold);
        logger.debug("Found {} books with low stock", books.size());
        return bookMapper.toDtoList(books);
    }

    @Override
    public BookDto updateStock(Long id, Integer quantity) {
        logger.debug("Updating stock for book id: {} by quantity: {}", id, quantity);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error(BOOK_NOT_FOUND_LOG, id);
                    return new ResourceNotFoundException(BOOK_NOT_FOUND_MSG + id);
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
        return bookMapper.toDto(updatedBook);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookDto> findAllPaginated(PageRequestDto pageRequest) {
        logger.debug("Fetching paginated books - page: {}, size: {}, sortBy: {}, sortDir: {}",
                pageRequest.getPage(), pageRequest.getSize(), pageRequest.getSortBy(), pageRequest.getSortDir());
        
        Pageable pageable = PageMapper.toPageable(pageRequest);
        Page<Book> bookPage = bookRepository.findAll(pageable);
        
        List<BookDto> bookDtos = bookPage.getContent().stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
        
        PageResponseDto<BookDto> response = PageMapper.toPageResponse(bookPage, BookDto.class);
        response.setContent(bookDtos);
        
        logger.debug("Returning {} books out of {} total", bookDtos.size(), bookPage.getTotalElements());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookDto> findByCategoryPaginated(String category, PageRequestDto pageRequest) {
        logger.debug("Fetching paginated books for category: {} - page: {}, size: {}",
                category, pageRequest.getPage(), pageRequest.getSize());
        
        Pageable pageable = PageMapper.toPageable(pageRequest);
        Page<Book> bookPage = bookRepository.findByCategory(category, pageable);
        
        List<BookDto> bookDtos = bookPage.getContent().stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
        
        PageResponseDto<BookDto> response = PageMapper.toPageResponse(bookPage, BookDto.class);
        response.setContent(bookDtos);
        
        logger.debug("Returning {} books in category '{}' out of {} total",
                bookDtos.size(), category, bookPage.getTotalElements());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<BookDto> searchPaginated(String searchTerm, PageRequestDto pageRequest) {
        logger.debug("Searching paginated books with term: {} - page: {}, size: {}",
                searchTerm, pageRequest.getPage(), pageRequest.getSize());
        
        Pageable pageable = PageMapper.toPageable(pageRequest);
        Page<Book> bookPage = bookRepository.searchByTitleOrAuthor(searchTerm, pageable);
        
        List<BookDto> bookDtos = bookPage.getContent().stream()
                .map(bookMapper::toDto)
                .collect(Collectors.toList());
        
        PageResponseDto<BookDto> response = PageMapper.toPageResponse(bookPage, BookDto.class);
        response.setContent(bookDtos);
        
        logger.debug("Returning {} books matching '{}' out of {} total",
                bookDtos.size(), searchTerm, bookPage.getTotalElements());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookDto> findByIds(List<Long> ids) {
        logger.debug("Fetching books by IDs: {}", ids);
        List<Book> books = bookRepository.findAllById(ids);
        logger.debug("Found {} books out of {} requested IDs", books.size(), ids.size());
        return bookMapper.toDtoList(books);
    }
}
