package com.bookstore.catalog.service;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.fixtures.BookTestFixtures;
import com.bookstore.catalog.mapper.BookMapper;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.common.dto.BookDto;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookService using Mockito.
 * Tests business logic in isolation from database and other dependencies.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Unit Tests")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book sampleBook;
    private BookDto sampleBookDto;

    @BeforeEach
    void setUp() {
        sampleBook = BookTestFixtures.createSampleBook();
        sampleBookDto = BookTestFixtures.createSampleBookDto();
    }

    @Test
    @DisplayName("findAll should return list of all books")
    void findAll_ShouldReturnAllBooks() {
        // Arrange
        List<Book> books = Arrays.asList(
                BookTestFixtures.createSampleBook(),
                BookTestFixtures.createSecondSampleBook()
        );
        List<BookDto> bookDtos = Arrays.asList(
                BookTestFixtures.createSampleBookDto(),
                BookTestFixtures.createSampleBookDto()
        );

        when(bookRepository.findAll()).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(bookDtos);

        // Act
        List<BookDto> result = bookService.findAll();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        verify(bookRepository, times(1)).findAll();
        verify(bookMapper, times(1)).toDtoList(books);
    }

    @Test
    @DisplayName("findById should return book when found")
    void findById_WhenBookExists_ShouldReturnBook() {
        // Arrange
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(sampleBook));
        when(bookMapper.toDto(sampleBook)).thenReturn(sampleBookDto);

        // Act
        BookDto result = bookService.findById(bookId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, times(1)).toDto(sampleBook);
    }

    @Test
    @DisplayName("findById should throw exception when book not found")
    void findById_WhenBookNotFound_ShouldThrowException() {
        // Arrange
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.findById(bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + bookId);

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("findByIsbn should return book when found")
    void findByIsbn_WhenBookExists_ShouldReturnBook() {
        // Arrange
        String isbn = "9780134685";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(sampleBook));
        when(bookMapper.toDto(sampleBook)).thenReturn(sampleBookDto);

        // Act
        BookDto result = bookService.findByIsbn(isbn);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getIsbn()).isEqualTo(isbn);
        verify(bookRepository, times(1)).findByIsbn(isbn);
        verify(bookMapper, times(1)).toDto(sampleBook);
    }

    @Test
    @DisplayName("findByIsbn should throw exception when book not found")
    void findByIsbn_WhenBookNotFound_ShouldThrowException() {
        // Arrange
        String isbn = "978-9999999999";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.findByIsbn(isbn))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with ISBN: " + isbn);

        verify(bookRepository, times(1)).findByIsbn(isbn);
    }

    @Test
    @DisplayName("create should save and return new book")
    void create_WithValidData_ShouldCreateBook() {
        // Arrange
        BookDto newBookDto = BookTestFixtures.createNewBookDto();
        Book newBook = BookTestFixtures.createSampleBook();

        when(bookRepository.existsByIsbn(newBookDto.getIsbn())).thenReturn(false);
        when(bookMapper.toEntity(newBookDto)).thenReturn(newBook);
        when(bookRepository.save(newBook)).thenReturn(newBook);
        when(bookMapper.toDto(newBook)).thenReturn(newBookDto);

        // Act
        BookDto result = bookService.create(newBookDto);

        // Assert
        assertThat(result).isNotNull();
        verify(bookRepository, times(1)).existsByIsbn(newBookDto.getIsbn());
        verify(bookRepository, times(1)).save(newBook);
        verify(bookMapper, times(1)).toEntity(newBookDto);
        verify(bookMapper, times(1)).toDto(newBook);
    }

    @Test
    @DisplayName("create should throw exception when ISBN already exists")
    void create_WithDuplicateIsbn_ShouldThrowException() {
        // Arrange
        BookDto newBookDto = BookTestFixtures.createNewBookDto();
        when(bookRepository.existsByIsbn(newBookDto.getIsbn())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bookService.create(newBookDto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already exists");

        verify(bookRepository, times(1)).existsByIsbn(newBookDto.getIsbn());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should update and return book when found")
    void update_WhenBookExists_ShouldUpdateBook() {
        // Arrange
        Long bookId = 1L;
        BookDto updateDto = BookTestFixtures.createUpdateBookDto();
        Book existingBook = BookTestFixtures.createSampleBook();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);
        when(bookMapper.toDto(existingBook)).thenReturn(updateDto);
        doNothing().when(bookMapper).updateEntityFromDto(updateDto, existingBook);

        // Act
        BookDto result = bookService.update(bookId, updateDto);

        // Assert
        assertThat(result).isNotNull();
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(existingBook);
        verify(bookMapper, times(1)).updateEntityFromDto(updateDto, existingBook);
    }

    @Test
    @DisplayName("update should throw exception when book not found")
    void update_WhenBookNotFound_ShouldThrowException() {
        // Arrange
        Long bookId = 999L;
        BookDto updateDto = BookTestFixtures.createUpdateBookDto();
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.update(bookId, updateDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + bookId);

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("update should throw exception when changing to duplicate ISBN")
    void update_WithDuplicateIsbn_ShouldThrowException() {
        // Arrange
        Long bookId = 1L;
        BookDto updateDto = BookTestFixtures.createUpdateBookDto();
        updateDto.setIsbn("978-9999999999");  // Different ISBN
        Book existingBook = BookTestFixtures.createSampleBook();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByIsbn(updateDto.getIsbn())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> bookService.update(bookId, updateDto))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already exists");

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).existsByIsbn(updateDto.getIsbn());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete should remove book when found")
    void delete_WhenBookExists_ShouldDeleteBook() {
        // Arrange
        Long bookId = 1L;
        when(bookRepository.existsById(bookId)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(bookId);

        // Act
        bookService.delete(bookId);

        // Assert
        verify(bookRepository, times(1)).existsById(bookId);
        verify(bookRepository, times(1)).deleteById(bookId);
    }

    @Test
    @DisplayName("delete should throw exception when book not found")
    void delete_WhenBookNotFound_ShouldThrowException() {
        // Arrange
        Long bookId = 999L;
        when(bookRepository.existsById(bookId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> bookService.delete(bookId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + bookId);

        verify(bookRepository, times(1)).existsById(bookId);
        verify(bookRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("findByCategory should return books in category")
    void findByCategory_ShouldReturnBooksInCategory() {
        // Arrange
        String category = "Programming";
        List<Book> books = Arrays.asList(sampleBook);
        List<BookDto> bookDtos = Arrays.asList(sampleBookDto);

        when(bookRepository.findByCategory(category)).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(bookDtos);

        // Act
        List<BookDto> result = bookService.findByCategory(category);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).findByCategory(category);
    }

    @Test
    @DisplayName("findByAuthor should return books by author")
    void findByAuthor_ShouldReturnBooksByAuthor() {
        // Arrange
        String author = "Joshua";
        List<Book> books = Arrays.asList(sampleBook);
        List<BookDto> bookDtos = Arrays.asList(sampleBookDto);

        when(bookRepository.findByAuthorContainingIgnoreCase(author)).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(bookDtos);

        // Act
        List<BookDto> result = bookService.findByAuthor(author);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).findByAuthorContainingIgnoreCase(author);
    }

    @Test
    @DisplayName("search should return books matching search term")
    void search_ShouldReturnMatchingBooks() {
        // Arrange
        String searchTerm = "Java";
        List<Book> books = Arrays.asList(sampleBook);
        List<BookDto> bookDtos = Arrays.asList(sampleBookDto);

        when(bookRepository.searchByTitleOrAuthor(searchTerm)).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(bookDtos);

        // Act
        List<BookDto> result = bookService.search(searchTerm);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).searchByTitleOrAuthor(searchTerm);
    }

    @Test
    @DisplayName("findAvailableBooks should return books with stock greater than zero")
    void findAvailableBooks_ShouldReturnInStockBooks() {
        // Arrange
        List<Book> books = Arrays.asList(sampleBook);
        List<BookDto> bookDtos = Arrays.asList(sampleBookDto);

        when(bookRepository.findByStockGreaterThan(0)).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(bookDtos);

        // Act
        List<BookDto> result = bookService.findAvailableBooks();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).findByStockGreaterThan(0);
    }

    @Test
    @DisplayName("findLowStockBooks should return books below threshold")
    void findLowStockBooks_ShouldReturnLowStockBooks() {
        // Arrange
        Integer threshold = 10;
        List<Book> books = Arrays.asList(BookTestFixtures.createLowStockBook());
        List<BookDto> bookDtos = Arrays.asList(sampleBookDto);

        when(bookRepository.findLowStockBooks(threshold)).thenReturn(books);
        when(bookMapper.toDtoList(books)).thenReturn(bookDtos);

        // Act
        List<BookDto> result = bookService.findLowStockBooks(threshold);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        verify(bookRepository, times(1)).findLowStockBooks(threshold);
    }

    @Test
    @DisplayName("updateStock should increase stock when quantity is positive")
    void updateStock_WithPositiveQuantity_ShouldIncreaseStock() {
        // Arrange
        Long bookId = 1L;
        Integer quantity = 50;
        Book book = BookTestFixtures.createSampleBook();
        book.setStock(100);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(sampleBookDto);

        // Act
        BookDto result = bookService.updateStock(bookId, quantity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(book.getStock()).isEqualTo(150);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    @DisplayName("updateStock should decrease stock when quantity is negative")
    void updateStock_WithNegativeQuantity_ShouldDecreaseStock() {
        // Arrange
        Long bookId = 1L;
        Integer quantity = -30;
        Book book = BookTestFixtures.createSampleBook();
        book.setStock(100);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(sampleBookDto);

        // Act
        BookDto result = bookService.updateStock(bookId, quantity);

        // Assert
        assertThat(result).isNotNull();
        assertThat(book.getStock()).isEqualTo(70);
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    @DisplayName("updateStock should throw exception when resulting stock would be negative")
    void updateStock_WhenResultingStockNegative_ShouldThrowException() {
        // Arrange
        Long bookId = 1L;
        Integer quantity = -150;
        Book book = BookTestFixtures.createSampleBook();
        book.setStock(100);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act & Assert
        assertThatThrownBy(() -> bookService.updateStock(bookId, quantity))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("Insufficient stock");

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStock should throw exception when book not found")
    void updateStock_WhenBookNotFound_ShouldThrowException() {
        // Arrange
        Long bookId = 999L;
        Integer quantity = 10;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.updateStock(bookId, quantity))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + bookId);

        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any());
    }
}
