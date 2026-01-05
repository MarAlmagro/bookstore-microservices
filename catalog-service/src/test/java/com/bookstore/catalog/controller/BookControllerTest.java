package com.bookstore.catalog.controller;

import com.bookstore.catalog.fixtures.BookTestFixtures;
import com.bookstore.catalog.service.BookService;
import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for BookController using MockMvc.
 * Tests REST endpoint behavior without starting the full Spring context.
 */
@WebMvcTest(BookController.class)
@DisplayName("BookController Integration Tests")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private BookDTO sampleBookDTO;

    @BeforeEach
    void setUp() {
        sampleBookDTO = BookTestFixtures.createSampleBookDTO();
    }

    @Test
    @DisplayName("GET /api/v1/books should return all books")
    void getAllBooks_ShouldReturnBooksList() throws Exception {
        // Arrange
        List<BookDTO> books = Arrays.asList(
                sampleBookDTO,
                BookTestFixtures.createSampleBookDTO()
        );
        when(bookService.findAll()).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)));

        verify(bookService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/books should return empty list when no books exist")
    void getAllBooks_WhenNoBooksExist_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(bookService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookService, times(1)).findAll();
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} should return book when found")
    void getBookById_WhenBookExists_ShouldReturnBook() throws Exception {
        // Arrange
        Long bookId = 1L;
        when(bookService.findById(bookId)).thenReturn(sampleBookDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(sampleBookDTO.getId().intValue())))
                .andExpect(jsonPath("$.isbn", is(sampleBookDTO.getIsbn())))
                .andExpect(jsonPath("$.title", is(sampleBookDTO.getTitle())))
                .andExpect(jsonPath("$.author", is(sampleBookDTO.getAuthor())));

        verify(bookService, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("GET /api/v1/books/{id} should return 404 when book not found")
    void getBookById_WhenBookNotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long bookId = 999L;
        when(bookService.findById(bookId))
                .thenThrow(new ResourceNotFoundException("Book not found with id: " + bookId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).findById(bookId);
    }

    @Test
    @DisplayName("GET /api/v1/books/isbn/{isbn} should return book when found")
    void getBookByIsbn_WhenBookExists_ShouldReturnBook() throws Exception {
        // Arrange
        String isbn = "9780134685";
        when(bookService.findByIsbn(isbn)).thenReturn(sampleBookDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/isbn/{isbn}", isbn))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is(isbn)))
                .andExpect(jsonPath("$.title", is(sampleBookDTO.getTitle())));

        verify(bookService, times(1)).findByIsbn(isbn);
    }

    @Test
    @DisplayName("POST /api/v1/books should create and return new book")
    void createBook_WithValidData_ShouldReturnCreatedBook() throws Exception {
        // Arrange
        BookDTO newBookDTO = BookTestFixtures.createNewBookDTO();
        BookDTO createdBookDTO = BookTestFixtures.createNewBookDTO();
        createdBookDTO.setId(1L);

        when(bookService.create(any(BookDTO.class))).thenReturn(createdBookDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookDTO)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(createdBookDTO.getId().intValue())))
                .andExpect(jsonPath("$.isbn", is(newBookDTO.getIsbn())))
                .andExpect(jsonPath("$.title", is(newBookDTO.getTitle())));

        verify(bookService, times(1)).create(any(BookDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/books should return 400 when validation fails")
    void createBook_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        BookDTO invalidBookDTO = BookTestFixtures.createInvalidBookDTO();

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBookDTO)))
                .andExpect(status().isBadRequest());

        verify(bookService, never()).create(any(BookDTO.class));
    }

    @Test
    @DisplayName("POST /api/v1/books should return 400 when ISBN already exists")
    void createBook_WithDuplicateIsbn_ShouldReturn400() throws Exception {
        // Arrange
        BookDTO newBookDTO = BookTestFixtures.createNewBookDTO();
        when(bookService.create(any(BookDTO.class)))
                .thenThrow(new InvalidRequestException("Book with ISBN already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookDTO)))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).create(any(BookDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} should update and return book")
    void updateBook_WithValidData_ShouldReturnUpdatedBook() throws Exception {
        // Arrange
        Long bookId = 1L;
        BookDTO updateDTO = BookTestFixtures.createUpdateBookDTO();

        when(bookService.update(eq(bookId), any(BookDTO.class))).thenReturn(updateDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(bookId.intValue())))
                .andExpect(jsonPath("$.title", is(updateDTO.getTitle())));

        verify(bookService, times(1)).update(eq(bookId), any(BookDTO.class));
    }

    @Test
    @DisplayName("PUT /api/v1/books/{id} should return 404 when book not found")
    void updateBook_WhenBookNotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long bookId = 999L;
        BookDTO updateDTO = BookTestFixtures.createUpdateBookDTO();

        when(bookService.update(eq(bookId), any(BookDTO.class)))
                .thenThrow(new ResourceNotFoundException("Book not found with id: " + bookId));

        // Act & Assert
        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).update(eq(bookId), any(BookDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} should return 204 when book deleted")
    void deleteBook_WhenBookExists_ShouldReturn204() throws Exception {
        // Arrange
        Long bookId = 1L;
        doNothing().when(bookService).delete(bookId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/books/{id}", bookId))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).delete(bookId);
    }

    @Test
    @DisplayName("DELETE /api/v1/books/{id} should return 404 when book not found")
    void deleteBook_WhenBookNotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long bookId = 999L;
        doThrow(new ResourceNotFoundException("Book not found with id: " + bookId))
                .when(bookService).delete(bookId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/books/{id}", bookId))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).delete(bookId);
    }

    @Test
    @DisplayName("GET /api/v1/books/search should return matching books")
    void searchBooks_ShouldReturnMatchingBooks() throws Exception {
        // Arrange
        String searchTerm = "Java";
        List<BookDTO> books = Arrays.asList(sampleBookDTO);

        when(bookService.search(searchTerm)).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search")
                        .param("searchTerm", searchTerm))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is(sampleBookDTO.getTitle())));

        verify(bookService, times(1)).search(searchTerm);
    }

    @Test
    @DisplayName("GET /api/v1/books/category/{category} should return books in category")
    void getBooksByCategory_ShouldReturnBooksInCategory() throws Exception {
        // Arrange
        String category = "Programming";
        List<BookDTO> books = Arrays.asList(sampleBookDTO);

        when(bookService.findByCategory(category)).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/category/{category}", category))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Programming")));

        verify(bookService, times(1)).findByCategory(category);
    }

    @Test
    @DisplayName("GET /api/v1/books/author/{author} should return books by author")
    void getBooksByAuthor_ShouldReturnBooksByAuthor() throws Exception {
        // Arrange
        String author = "Joshua";
        List<BookDTO> books = Arrays.asList(sampleBookDTO);

        when(bookService.findByAuthor(author)).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/author/{author}", author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", is(sampleBookDTO.getAuthor())));

        verify(bookService, times(1)).findByAuthor(author);
    }

    @Test
    @DisplayName("GET /api/v1/books/available should return available books")
    void getAvailableBooks_ShouldReturnBooksInStock() throws Exception {
        // Arrange
        List<BookDTO> books = Arrays.asList(sampleBookDTO);

        when(bookService.findAvailableBooks()).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookService, times(1)).findAvailableBooks();
    }

    @Test
    @DisplayName("GET /api/v1/books/low-stock should return low stock books")
    void getLowStockBooks_ShouldReturnLowStockBooks() throws Exception {
        // Arrange
        Integer threshold = 10;
        List<BookDTO> books = Arrays.asList(sampleBookDTO);

        when(bookService.findLowStockBooks(threshold)).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/low-stock")
                        .param("threshold", threshold.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookService, times(1)).findLowStockBooks(threshold);
    }

    @Test
    @DisplayName("GET /api/v1/books/low-stock should use default threshold when not provided")
    void getLowStockBooks_WithoutThreshold_ShouldUseDefault() throws Exception {
        // Arrange
        List<BookDTO> books = Arrays.asList(sampleBookDTO);

        when(bookService.findLowStockBooks(10)).thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(bookService, times(1)).findLowStockBooks(10);
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id}/stock should update stock")
    void updateStock_WithValidQuantity_ShouldReturnUpdatedBook() throws Exception {
        // Arrange
        Long bookId = 1L;
        Integer quantity = 50;
        BookDTO updatedBook = BookTestFixtures.createSampleBookDTO();
        updatedBook.setStock(150);

        when(bookService.updateStock(bookId, quantity)).thenReturn(updatedBook);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/books/{id}/stock", bookId)
                        .param("quantity", quantity.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(150)));

        verify(bookService, times(1)).updateStock(bookId, quantity);
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id}/stock should return 400 when stock would be negative")
    void updateStock_WhenResultingStockNegative_ShouldReturn400() throws Exception {
        // Arrange
        Long bookId = 1L;
        Integer quantity = -200;

        when(bookService.updateStock(bookId, quantity))
                .thenThrow(new InvalidRequestException("Insufficient stock"));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/books/{id}/stock", bookId)
                        .param("quantity", quantity.toString()))
                .andExpect(status().isBadRequest());

        verify(bookService, times(1)).updateStock(bookId, quantity);
    }

    @Test
    @DisplayName("PATCH /api/v1/books/{id}/stock should return 404 when book not found")
    void updateStock_WhenBookNotFound_ShouldReturn404() throws Exception {
        // Arrange
        Long bookId = 999L;
        Integer quantity = 10;

        when(bookService.updateStock(bookId, quantity))
                .thenThrow(new ResourceNotFoundException("Book not found with id: " + bookId));

        // Act & Assert
        mockMvc.perform(patch("/api/v1/books/{id}/stock", bookId)
                        .param("quantity", quantity.toString()))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).updateStock(bookId, quantity);
    }
}
