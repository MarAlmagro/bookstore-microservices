package com.bookstore.admin.controller;

import com.bookstore.admin.client.CatalogClient;
import com.bookstore.admin.dto.BookDto;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookController Unit Tests")
class BookControllerTest {

    private static final String ISBN1 = "ISBN1";
    private static final String BOOK_1 = "Book 1";
    private static final String AUTHOR_1 = "Author 1";
    private static final String DESC = "Desc";
    private static final String FICTION = "Fiction";
    private static final String TECHNOLOGY = "Technology";
    private static final String API_BOOKS = "/api/books";
    private static final String API_BOOKS_999 = "/api/books/999";
    private static final String JAVA = "Java";
    private static final String BOOKS_LIST = "books/list";
    private static final String REDIRECT_ADMIN_BOOKS = "redirect:/admin/books";
    private static final String SUCCESS = "success";
    private static final String ERROR = "error";

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private BookController bookController;

    @Test
    @DisplayName("listBooks should return all books when no query provided")
    void testListBooks() {
        List<BookDto> books = new ArrayList<>();
        books.add(new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION));

        when(catalogClient.getAllBooks()).thenReturn(books);

        String viewName = bookController.listBooks(null, model);

        assertThat(viewName).isEqualTo(BOOKS_LIST);
        verify(model).addAttribute("books", books);
    }

    @Test
    @DisplayName("newBookForm should return form view with empty book")
    void testNewBookForm() {
        String viewName = bookController.newBookForm(model);

        assertThat(viewName).isEqualTo("books/form");
        verify(model).addAttribute(eq("book"), any(BookDto.class));
    }

    @Test
    @DisplayName("createBook should redirect with success message")
    void testCreateBook() {
        BookDto book = new BookDto(null, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION);

        when(catalogClient.createBook(book)).thenReturn(book);

        String viewName = bookController.createBook(book, redirectAttributes);

        assertThat(viewName).isEqualTo(REDIRECT_ADMIN_BOOKS);
        verify(redirectAttributes).addFlashAttribute(SUCCESS, "books.success.created");
    }

    @Test
    @DisplayName("updateBook should redirect with success message")
    void testUpdateBook() {
        BookDto book = new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION);

        when(catalogClient.updateBook(1L, book)).thenReturn(book);

        String viewName = bookController.updateBook(1L, book, redirectAttributes);

        assertThat(viewName).isEqualTo(REDIRECT_ADMIN_BOOKS);
        verify(redirectAttributes).addFlashAttribute(SUCCESS, "books.success.updated");
    }

    @Test
    @DisplayName("deleteBook should redirect with success message")
    void testDeleteBook() {
        doNothing().when(catalogClient).deleteBook(1L);

        String viewName = bookController.deleteBook(1L, redirectAttributes);

        assertThat(viewName).isEqualTo(REDIRECT_ADMIN_BOOKS);
        verify(redirectAttributes).addFlashAttribute(SUCCESS, "books.success.deleted");
    }

    @Test
    @DisplayName("listBooks should handle search query")
    void listBooks_withSearchQuery_shouldReturnFilteredBooks() {
        List<BookDto> books = List.of(
            new BookDto(1L, ISBN1, "Java Programming", AUTHOR_1, DESC, BigDecimal.TEN, 10, TECHNOLOGY)
        );

        when(catalogClient.searchBooks(JAVA)).thenReturn(books);

        String viewName = bookController.listBooks(JAVA, model);

        assertThat(viewName).isEqualTo(BOOKS_LIST);
        verify(catalogClient).searchBooks(JAVA);
        verify(catalogClient, never()).getAllBooks();
        verify(model).addAttribute("books", books);
        verify(model).addAttribute("query", JAVA);
    }

    @Test
    @DisplayName("listBooks should handle FeignException and add error to model")
    void listBooks_whenFeignException_shouldAddErrorToModel() {
        Request request = Request.create(Request.HttpMethod.GET, API_BOOKS, new HashMap<>(), null, new RequestTemplate());
        FeignException exception = new FeignException.ServiceUnavailable("Service unavailable", request, null, null);

        when(catalogClient.getAllBooks()).thenThrow(exception);

        String viewName = bookController.listBooks(null, model);

        assertThat(viewName).isEqualTo(BOOKS_LIST);
        verify(model).addAttribute(eq(ERROR), contains("Failed to load books"));
    }

    @Test
    @DisplayName("listBooks should handle empty search query as null")
    void listBooks_withEmptyQuery_shouldCallGetAllBooks() {
        List<BookDto> books = List.of(
            new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION)
        );

        when(catalogClient.getAllBooks()).thenReturn(books);

        String viewName = bookController.listBooks("", model);

        assertThat(viewName).isEqualTo(BOOKS_LIST);
        verify(catalogClient).getAllBooks();
        verify(catalogClient, never()).searchBooks(anyString());
    }

    @Test
    @DisplayName("editBookForm should return book when found")
    void editBookForm_whenBookFound_shouldReturnFormView() {
        BookDto book = new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION);

        when(catalogClient.getBookById(1L)).thenReturn(book);

        String viewName = bookController.editBookForm(1L, model);

        assertThat(viewName).isEqualTo("books/form");
        verify(model).addAttribute("book", book);
    }

    @Test
    @DisplayName("editBookForm should redirect when book not found")
    void editBookForm_whenBookNotFound_shouldRedirect() {
        Request request = Request.create(Request.HttpMethod.GET, API_BOOKS_999, new HashMap<>(), null, new RequestTemplate());
        FeignException exception = new FeignException.NotFound("Not found", request, null, null);

        when(catalogClient.getBookById(999L)).thenThrow(exception);

        String viewName = bookController.editBookForm(999L, model);

        assertThat(viewName).isEqualTo(REDIRECT_ADMIN_BOOKS);
        verify(model).addAttribute(ERROR, "books.error.notFound");
    }

    @Test
    @DisplayName("createBook should handle FeignException and add error message")
    void createBook_whenFeignException_shouldAddErrorMessage() {
        BookDto book = new BookDto(null, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION);
        Request request = Request.create(Request.HttpMethod.POST, API_BOOKS, new HashMap<>(), null, new RequestTemplate());
        FeignException exception = new FeignException.BadRequest("Validation failed", request, null, null);

        when(catalogClient.createBook(book)).thenThrow(exception);

        String viewName = bookController.createBook(book, redirectAttributes);

        assertThat(viewName).isEqualTo(REDIRECT_ADMIN_BOOKS);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR), contains("Failed to create book"));
    }

    @Test
    @DisplayName("updateBook should handle FeignException and add error message")
    void updateBook_whenFeignException_shouldAddErrorMessage() {
        BookDto book = new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION);
        Request request = Request.create(Request.HttpMethod.PUT, "/api/books/1", new HashMap<>(), null, new RequestTemplate());
        FeignException exception = new FeignException.NotFound("Book not found", request, null, null);

        when(catalogClient.updateBook(1L, book)).thenThrow(exception);

        String viewName = bookController.updateBook(1L, book, redirectAttributes);

        assertThat(viewName).isEqualTo(REDIRECT_ADMIN_BOOKS);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR), contains("Failed to update book"));
    }

    @Test
    @DisplayName("deleteBook should handle FeignException and add error message")
    void deleteBook_whenFeignException_shouldAddErrorMessage() {
        Request request = Request.create(Request.HttpMethod.DELETE, API_BOOKS_999, new HashMap<>(), null, new RequestTemplate());
        FeignException exception = new FeignException.NotFound("Book not found", request, null, null);

        doThrow(exception).when(catalogClient).deleteBook(999L);

        String viewName = bookController.deleteBook(999L, redirectAttributes);

        assertThat(viewName).isEqualTo(REDIRECT_ADMIN_BOOKS);
        verify(redirectAttributes).addFlashAttribute(eq(ERROR), contains("Failed to delete book"));
    }

    @Test
    @DisplayName("listBooks should handle generic RuntimeException")
    void listBooks_whenRuntimeException_shouldAddErrorToModel() {
        when(catalogClient.getAllBooks()).thenThrow(new RuntimeException("Unexpected error"));

        String viewName = bookController.listBooks(null, model);

        assertThat(viewName).isEqualTo(BOOKS_LIST);
        verify(model).addAttribute(eq(ERROR), contains("Failed to load books"));
    }
}
