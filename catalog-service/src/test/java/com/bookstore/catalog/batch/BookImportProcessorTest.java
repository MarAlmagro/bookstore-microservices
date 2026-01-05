package com.bookstore.catalog.batch;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.common.dto.BookImportDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookImportProcessorTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookImportProcessor processor;

    private BookImportDTO importDTO;

    @BeforeEach
    void setUp() {
        importDTO = BookImportDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Test Book")
                .author("Test Author")
                .description("Test Description")
                .price(new BigDecimal("29.99"))
                .stock(100)
                .category("Fiction")
                .build();
    }

    @Test
    void process_ShouldCreateNewBook_WhenIsbnDoesNotExist() throws Exception {
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        Book result = processor.process(importDTO);

        assertNotNull(result);
        assertEquals(importDTO.getIsbn(), result.getIsbn());
        assertEquals(importDTO.getTitle(), result.getTitle());
        assertEquals(importDTO.getAuthor(), result.getAuthor());
        assertEquals(importDTO.getDescription(), result.getDescription());
        assertEquals(importDTO.getPrice(), result.getPrice());
        assertEquals(importDTO.getStock(), result.getStock());
        assertEquals(importDTO.getCategory(), result.getCategory());
        
        verify(bookRepository, times(1)).findByIsbn(importDTO.getIsbn());
    }

    @Test
    void process_ShouldUpdateExistingBook_WhenIsbnExists() throws Exception {
        Book existingBook = Book.builder()
                .id(1L)
                .isbn("978-0-123456-78-9")
                .title("Old Title")
                .author("Old Author")
                .description("Old Description")
                .price(new BigDecimal("19.99"))
                .stock(50)
                .category("NonFiction")
                .build();

        when(bookRepository.findByIsbn(importDTO.getIsbn())).thenReturn(Optional.of(existingBook));

        Book result = processor.process(importDTO);

        assertNotNull(result);
        assertEquals(existingBook.getId(), result.getId());
        assertEquals(importDTO.getIsbn(), result.getIsbn());
        assertEquals(importDTO.getTitle(), result.getTitle());
        assertEquals(importDTO.getAuthor(), result.getAuthor());
        assertEquals(importDTO.getDescription(), result.getDescription());
        assertEquals(importDTO.getPrice(), result.getPrice());
        assertEquals(importDTO.getStock(), result.getStock());
        assertEquals(importDTO.getCategory(), result.getCategory());
        
        verify(bookRepository, times(1)).findByIsbn(importDTO.getIsbn());
    }

    @Test
    void process_ShouldUpdatePriceAndStock_WhenBookExists() throws Exception {
        Book existingBook = Book.builder()
                .id(2L)
                .isbn("978-0-123456-78-9")
                .title("Existing Title")
                .author("Existing Author")
                .description("Existing Description")
                .price(new BigDecimal("15.00"))
                .stock(25)
                .category("Science")
                .build();

        BookImportDTO updatedDTO = BookImportDTO.builder()
                .isbn("978-0-123456-78-9")
                .title("Updated Title")
                .author("Updated Author")
                .description("Updated Description")
                .price(new BigDecimal("35.00"))
                .stock(150)
                .category("Technology")
                .build();

        when(bookRepository.findByIsbn(updatedDTO.getIsbn())).thenReturn(Optional.of(existingBook));

        Book result = processor.process(updatedDTO);

        assertEquals(new BigDecimal("35.00"), result.getPrice());
        assertEquals(150, result.getStock());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Author", result.getAuthor());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("Technology", result.getCategory());
    }

    @Test
    void process_ShouldHandleZeroStock_ForNewBook() throws Exception {
        importDTO.setStock(0);
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        Book result = processor.process(importDTO);

        assertNotNull(result);
        assertEquals(0, result.getStock());
    }

    @Test
    void process_ShouldHandleZeroPrice_ForNewBook() throws Exception {
        importDTO.setPrice(BigDecimal.ZERO);
        when(bookRepository.findByIsbn(anyString())).thenReturn(Optional.empty());

        Book result = processor.process(importDTO);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getPrice());
    }

    @Test
    void process_ShouldPreserveId_WhenUpdatingExistingBook() throws Exception {
        Long originalId = 999L;
        Book existingBook = Book.builder()
                .id(originalId)
                .isbn("978-0-123456-78-9")
                .title("Original")
                .author("Original")
                .description("Original")
                .price(new BigDecimal("10.00"))
                .stock(10)
                .category("Original")
                .build();

        when(bookRepository.findByIsbn(importDTO.getIsbn())).thenReturn(Optional.of(existingBook));

        Book result = processor.process(importDTO);

        assertEquals(originalId, result.getId());
    }
}
