package com.bookstore.admin.controller;

import com.bookstore.admin.client.CatalogClient;
import com.bookstore.admin.dto.BookDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class BookControllerTest {

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListBooks() {
        List<BookDTO> books = new ArrayList<>();
        books.add(new BookDTO(1L, "ISBN1", "Book 1", "Author 1", "Desc", BigDecimal.TEN, 10, "Fiction"));

        when(catalogClient.getAllBooks()).thenReturn(books);

        String viewName = bookController.listBooks(null, model);

        assertEquals("books/list", viewName);
        verify(model).addAttribute("books", books);
    }

    @Test
    void testNewBookForm() {
        String viewName = bookController.newBookForm(model);

        assertEquals("books/form", viewName);
        verify(model).addAttribute(eq("book"), any(BookDTO.class));
    }

    @Test
    void testCreateBook() {
        BookDTO book = new BookDTO(null, "ISBN1", "Book 1", "Author 1", "Desc", BigDecimal.TEN, 10, "Fiction");

        when(catalogClient.createBook(book)).thenReturn(book);

        String viewName = bookController.createBook(book, redirectAttributes);

        assertEquals("redirect:/admin/books", viewName);
        verify(redirectAttributes).addFlashAttribute("success", "books.success.created");
    }

    @Test
    void testUpdateBook() {
        BookDTO book = new BookDTO(1L, "ISBN1", "Book 1", "Author 1", "Desc", BigDecimal.TEN, 10, "Fiction");

        when(catalogClient.updateBook(1L, book)).thenReturn(book);

        String viewName = bookController.updateBook(1L, book, redirectAttributes);

        assertEquals("redirect:/admin/books", viewName);
        verify(redirectAttributes).addFlashAttribute("success", "books.success.updated");
    }

    @Test
    void testDeleteBook() {
        doNothing().when(catalogClient).deleteBook(1L);

        String viewName = bookController.deleteBook(1L, redirectAttributes);

        assertEquals("redirect:/admin/books", viewName);
        verify(redirectAttributes).addFlashAttribute("success", "books.success.deleted");
    }
}
