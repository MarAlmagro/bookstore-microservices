package com.bookstore.admin.client;

import com.bookstore.admin.dto.BookDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import feign.Feign;
import feign.FeignException;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.openfeign.support.SpringMvcContract;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CatalogClient Integration Tests")
class CatalogClientTest {

    private static final String ISBN_001 = "ISBN-001";
    private static final String ISBN_002 = "ISBN-002";
    private static final String ISBN_003 = "ISBN-003";
    private static final String BOOK_1 = "Book 1";
    private static final String BOOK_2 = "Book 2";
    private static final String AUTHOR_1 = "Author 1";
    private static final String AUTHOR_2 = "Author 2";
    private static final String DESCRIPTION_1 = "Description 1";
    private static final String DESCRIPTION_2 = "Description 2";
    private static final String FICTION = "Fiction";
    private static final String SCIENCE = "Science";
    private static final String TECHNOLOGY = "Technology";
    private static final String API_BOOKS = "/api/books";
    private static final String API_BOOKS_1 = "/api/books/1";
    private static final String API_BOOKS_999 = "/api/books/999";
    private static final String API_BOOKS_SEARCH = "/api/books/search";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String BOOK_NOT_FOUND = "Book not found";
    private static final String QUERY = "query";

    private CatalogClient catalogClient;
    private ObjectMapper objectMapper;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8089));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
        
        objectMapper = new ObjectMapper();
        catalogClient = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .target(CatalogClient.class, "http://localhost:8089/api/books");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("getAllBooks should return list of books when successful")
    void getAllBooks_whenSuccessful_shouldReturnBookList() throws Exception {
        BookDto book1 = new BookDto(1L, ISBN_001, BOOK_1, AUTHOR_1, DESCRIPTION_1, 
                                    BigDecimal.valueOf(29.99), 10, FICTION);
        BookDto book2 = new BookDto(2L, ISBN_002, BOOK_2, AUTHOR_2, DESCRIPTION_2, 
                                    BigDecimal.valueOf(39.99), 5, SCIENCE);
        List<BookDto> books = List.of(book1, book2);

        stubFor(get(urlEqualTo(API_BOOKS))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(books))));

        List<BookDto> result = catalogClient.getAllBooks();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo(BOOK_1);
        assertThat(result.get(1).getTitle()).isEqualTo(BOOK_2);
        verify(getRequestedFor(urlEqualTo(API_BOOKS)));
    }

    @Test
    @DisplayName("getAllBooks should throw FeignException when server error occurs")
    void getAllBooks_whenServerError_shouldThrowFeignException() {
        stubFor(get(urlEqualTo(API_BOOKS))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        assertThatThrownBy(() -> catalogClient.getAllBooks())
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("getBookById should return book when found")
    void getBookById_whenFound_shouldReturnBook() throws Exception {
        BookDto book = new BookDto(1L, ISBN_001, BOOK_1, AUTHOR_1, DESCRIPTION_1, 
                                   BigDecimal.valueOf(29.99), 10, FICTION);

        stubFor(get(urlEqualTo(API_BOOKS_1))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(book))));

        BookDto result = catalogClient.getBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo(BOOK_1);
        verify(getRequestedFor(urlEqualTo(API_BOOKS_1)));
    }

    @Test
    @DisplayName("getBookById should throw FeignException when not found")
    void getBookById_whenNotFound_shouldThrowFeignException() {
        stubFor(get(urlEqualTo(API_BOOKS_999))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(BOOK_NOT_FOUND)));

        assertThatThrownBy(() -> catalogClient.getBookById(999L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("createBook should return created book when successful")
    void createBook_whenSuccessful_shouldReturnCreatedBook() throws Exception {
        BookDto bookToCreate = new BookDto(null, ISBN_003, "New Book", "New Author", "New Description", 
                                           BigDecimal.valueOf(49.99), 15, TECHNOLOGY);
        BookDto createdBook = new BookDto(3L, ISBN_003, "New Book", "New Author", "New Description", 
                                          BigDecimal.valueOf(49.99), 15, TECHNOLOGY);

        stubFor(post(urlEqualTo(API_BOOKS))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(bookToCreate)))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(createdBook))));

        BookDto result = catalogClient.createBook(bookToCreate);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getIsbn()).isEqualTo(ISBN_003);
        verify(postRequestedFor(urlEqualTo(API_BOOKS)));
    }

    @Test
    @DisplayName("createBook should throw FeignException when validation fails")
    void createBook_whenValidationFails_shouldThrowFeignException() throws Exception {
        BookDto invalidBook = new BookDto(null, "", "", "", "", null, null, "");

        stubFor(post(urlEqualTo(API_BOOKS))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Validation failed")));

        assertThatThrownBy(() -> catalogClient.createBook(invalidBook))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("updateBook should return updated book when successful")
    void updateBook_whenSuccessful_shouldReturnUpdatedBook() throws Exception {
        BookDto bookToUpdate = new BookDto(1L, ISBN_001, "Updated Book", "Updated Author", "Updated Description", 
                                           BigDecimal.valueOf(59.99), 20, FICTION);

        stubFor(put(urlEqualTo(API_BOOKS_1))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(bookToUpdate)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(bookToUpdate))));

        BookDto result = catalogClient.updateBook(1L, bookToUpdate);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Book");
        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(59.99));
        verify(putRequestedFor(urlEqualTo(API_BOOKS_1)));
    }

    @Test
    @DisplayName("updateBook should throw FeignException when book not found")
    void updateBook_whenNotFound_shouldThrowFeignException() throws Exception {
        BookDto bookToUpdate = new BookDto(999L, "ISBN-999", "Non-existent", "Author", "Description", 
                                           BigDecimal.valueOf(29.99), 10, FICTION);

        stubFor(put(urlEqualTo(API_BOOKS_999))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(BOOK_NOT_FOUND)));

        assertThatThrownBy(() -> catalogClient.updateBook(999L, bookToUpdate))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("deleteBook should complete successfully when book exists")
    void deleteBook_whenBookExists_shouldCompleteSuccessfully() {
        stubFor(delete(urlEqualTo(API_BOOKS_1))
                .willReturn(aResponse()
                        .withStatus(204)));

        catalogClient.deleteBook(1L);

        verify(deleteRequestedFor(urlEqualTo(API_BOOKS_1)));
    }

    @Test
    @DisplayName("deleteBook should throw FeignException when book not found")
    void deleteBook_whenNotFound_shouldThrowFeignException() {
        stubFor(delete(urlEqualTo(API_BOOKS_999))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(BOOK_NOT_FOUND)));

        assertThatThrownBy(() -> catalogClient.deleteBook(999L))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("searchBooks should return matching books when found")
    void searchBooks_whenFound_shouldReturnMatchingBooks() throws Exception {
        BookDto book1 = new BookDto(1L, ISBN_001, "Java Programming", AUTHOR_1, DESCRIPTION_1, 
                                    BigDecimal.valueOf(29.99), 10, TECHNOLOGY);
        BookDto book2 = new BookDto(2L, ISBN_002, "Advanced Java", AUTHOR_2, DESCRIPTION_2, 
                                    BigDecimal.valueOf(39.99), 5, TECHNOLOGY);
        List<BookDto> books = List.of(book1, book2);

        stubFor(get(urlPathEqualTo(API_BOOKS_SEARCH))
                .withQueryParam(QUERY, equalTo("Java"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(books))));

        List<BookDto> result = catalogClient.searchBooks("Java");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).contains("Java");
        assertThat(result.get(1).getTitle()).contains("Java");
        verify(getRequestedFor(urlPathEqualTo(API_BOOKS_SEARCH))
                .withQueryParam(QUERY, equalTo("Java")));
    }

    @Test
    @DisplayName("searchBooks should return empty list when no matches found")
    void searchBooks_whenNoMatches_shouldReturnEmptyList() throws Exception {
        stubFor(get(urlPathEqualTo(API_BOOKS_SEARCH))
                .withQueryParam(QUERY, equalTo("NonExistent"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(List.of()))));

        List<BookDto> result = catalogClient.searchBooks("NonExistent");

        assertThat(result).isEmpty();
    }
}
