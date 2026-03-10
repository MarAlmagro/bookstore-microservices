package com.bookstore.catalog.contracts;

import com.bookstore.catalog.controller.BookController;
import com.bookstore.catalog.dto.BookDto;
import com.bookstore.catalog.service.BookService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@WebMvcTest(BookController.class)
@ActiveProfiles("test")
public abstract class ContractVerifierBase {

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private BookService bookService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.webAppContextSetup(context);

        BookDto mockBook = new BookDto();
        mockBook.setId(1L);
        mockBook.setIsbn("978-0134685991");
        mockBook.setTitle("Effective Java");
        mockBook.setAuthor("Joshua Bloch");
        mockBook.setPrice(BigDecimal.valueOf(45.99));
        mockBook.setStock(10);

        when(bookService.getBookById(anyLong())).thenReturn(mockBook);
        when(bookService.getAllBooks()).thenReturn(Collections.singletonList(mockBook));
    }
}
