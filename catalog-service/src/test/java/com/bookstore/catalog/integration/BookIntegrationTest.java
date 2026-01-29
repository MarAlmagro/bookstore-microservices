package com.bookstore.catalog.integration;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.fixtures.BookTestFixtures;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.common.dto.BookDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Catalog Service.
 * Tests the full application stack with H2 in-memory database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Book Integration Tests")
class BookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create, retrieve, update and delete a book (full CRUD)")
    void fullCrudLifecycle_ShouldWorkCorrectly() throws Exception {
        // Create a new book
        BookDto newBookDto = BookTestFixtures.createNewBookDto();

        String createResponse = mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBookDto)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.isbn", is(newBookDto.getIsbn())))
                .andExpect(jsonPath("$.title", is(newBookDto.getTitle())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookDto createdBook = objectMapper.readValue(createResponse, BookDto.class);
        Long bookId = createdBook.getId();

        // Retrieve the created book
        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookId.intValue())))
                .andExpect(jsonPath("$.title", is(newBookDto.getTitle())));

        // Update the book
        BookDto updateDto = BookTestFixtures.createUpdateBookDto();
        updateDto.setId(bookId);
        updateDto.setIsbn(newBookDto.getIsbn()); // Keep same ISBN

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookId.intValue())))
                .andExpect(jsonPath("$.title", is(updateDto.getTitle())));

        // Delete the book
        mockMvc.perform(delete("/api/v1/books/{id}", bookId))
                .andExpect(status().isNoContent());

        // Verify book is deleted
        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should retrieve all books")
    void getAllBooks_ShouldReturnAllBooksFromDatabase() throws Exception {
        // Arrange - Create test data
        Book book1 = BookTestFixtures.createSampleBook();
        book1.setId(null); // Let database generate ID
        Book book2 = BookTestFixtures.createSecondSampleBook();
        book2.setId(null);

        bookRepository.save(book1);
        bookRepository.save(book2);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder(book1.getTitle(), book2.getTitle())));
    }

    @Test
    @DisplayName("Should find book by ISBN")
    void getBookByIsbn_ShouldReturnCorrectBook() throws Exception {
        // Arrange
        Book book = BookTestFixtures.createSampleBook();
        book.setId(null);
        Book savedBook = bookRepository.save(book);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/isbn/{isbn}", savedBook.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn", is(savedBook.getIsbn())))
                .andExpect(jsonPath("$.title", is(savedBook.getTitle())));
    }

    @Test
    @DisplayName("Should not create book with duplicate ISBN")
    void createBook_WithDuplicateIsbn_ShouldFail() throws Exception {
        // Arrange - Create a book
        Book existingBook = BookTestFixtures.createSampleBook();
        existingBook.setId(null);
        bookRepository.save(existingBook);

        // Try to create another book with same ISBN
        BookDto duplicateBookDto = BookTestFixtures.createNewBookDto();
        duplicateBookDto.setIsbn(existingBook.getIsbn());

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateBookDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should search books by title or author")
    void searchBooks_ShouldReturnMatchingBooks() throws Exception {
        // Arrange
        Book book1 = BookTestFixtures.createSampleBook();
        book1.setId(null);
        book1.setTitle("Effective Java");
        book1.setAuthor("Joshua Bloch");

        Book book2 = BookTestFixtures.createSecondSampleBook();
        book2.setId(null);
        book2.setTitle("Clean Code");
        book2.setAuthor("Robert C. Martin");

        bookRepository.save(book1);
        bookRepository.save(book2);

        // Search by title
        mockMvc.perform(get("/api/v1/books/search")
                        .param("query", "Java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", containsString("Java")));

        // Search by author
        mockMvc.perform(get("/api/v1/books/search")
                        .param("query", "Martin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", containsString("Martin")));
    }

    @Test
    @DisplayName("Should filter books by category")
    void getBooksByCategory_ShouldReturnOnlyBooksInCategory() throws Exception {
        // Arrange
        Book programmingBook = BookTestFixtures.createSampleBook();
        programmingBook.setId(null);
        programmingBook.setCategory("Programming");

        Book architectureBook = BookTestFixtures.createOutOfStockBook();
        architectureBook.setId(null);
        architectureBook.setIsbn("978-9999999999");
        architectureBook.setCategory("Software Architecture");

        bookRepository.save(programmingBook);
        bookRepository.save(architectureBook);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/category/{category}", "Programming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].category", is("Programming")));
    }

    @Test
    @DisplayName("Should filter books by author")
    void getBooksByAuthor_ShouldReturnBooksMatchingAuthor() throws Exception {
        // Arrange
        Book book1 = BookTestFixtures.createSampleBook();
        book1.setId(null);
        book1.setAuthor("Joshua Bloch");

        Book book2 = BookTestFixtures.createSecondSampleBook();
        book2.setId(null);
        book2.setAuthor("Robert C. Martin");

        bookRepository.save(book1);
        bookRepository.save(book2);

        // Act & Assert - Partial match
        mockMvc.perform(get("/api/v1/books/author/{author}", "Joshua"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author", containsString("Joshua")));
    }

    @Test
    @DisplayName("Should return only available books")
    void getAvailableBooks_ShouldReturnOnlyBooksInStock() throws Exception {
        // Arrange
        Book inStockBook = BookTestFixtures.createSampleBook();
        inStockBook.setId(null);
        inStockBook.setStock(100);

        Book outOfStockBook = BookTestFixtures.createOutOfStockBook();
        outOfStockBook.setId(null);
        outOfStockBook.setIsbn("978-9999999999");
        outOfStockBook.setStock(0);

        bookRepository.save(inStockBook);
        bookRepository.save(outOfStockBook);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stock", greaterThan(0)));
    }

    @Test
    @DisplayName("Should return low stock books")
    void getLowStockBooks_ShouldReturnBooksbelowThreshold() throws Exception {
        // Arrange
        Book highStockBook = BookTestFixtures.createSampleBook();
        highStockBook.setId(null);
        highStockBook.setStock(100);

        Book lowStockBook = BookTestFixtures.createLowStockBook();
        lowStockBook.setId(null);
        lowStockBook.setIsbn("978-9999999999");
        lowStockBook.setStock(5);

        bookRepository.save(highStockBook);
        bookRepository.save(lowStockBook);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/low-stock")
                        .param("threshold", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stock", lessThan(10)));
    }

    @Test
    @DisplayName("Should update stock correctly")
    void updateStock_ShouldModifyStockQuantity() throws Exception {
        // Arrange
        Book book = BookTestFixtures.createSampleBook();
        book.setId(null);
        book.setStock(100);
        Book savedBook = bookRepository.save(book);

        // Act - Increase stock
        mockMvc.perform(patch("/api/v1/books/{id}/stock", savedBook.getId())
                        .param("quantity", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(150)));

        // Act - Decrease stock
        mockMvc.perform(patch("/api/v1/books/{id}/stock", savedBook.getId())
                        .param("quantity", "-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock", is(120)));
    }

    @Test
    @DisplayName("Should not allow stock to go negative")
    void updateStock_WhenResultingStockNegative_ShouldFail() throws Exception {
        // Arrange
        Book book = BookTestFixtures.createSampleBook();
        book.setId(null);
        book.setStock(50);
        Book savedBook = bookRepository.save(book);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/books/{id}/stock", savedBook.getId())
                        .param("quantity", "-100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should validate required fields when creating book")
    void createBook_WithMissingRequiredFields_ShouldFail() throws Exception {
        // Arrange - Book with missing required fields
        BookDto invalidBook = BookDto.builder()
                .isbn("")  // Invalid: empty
                .title("")  // Invalid: empty
                .price(new BigDecimal("-10"))  // Invalid: negative
                .stock(-5)  // Invalid: negative
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle concurrent book creation correctly")
    void createMultipleBooks_Concurrently_ShouldSucceed() throws Exception {
        // Arrange & Act - Create multiple books with different ISBNs
        for (int i = 1; i <= 5; i++) {
            BookDto book = BookTestFixtures.createNewBookDto();
            book.setIsbn("978123456" + i);  // Valid 10-character ISBN
            book.setTitle("Test Book " + i);

            mockMvc.perform(post("/api/v1/books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(book)))
                    .andExpect(status().isCreated());
        }

        // Assert - Verify all books were created
        mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }
}
