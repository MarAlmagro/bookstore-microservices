package com.bookstore.admin.controller;

import com.bookstore.admin.client.CatalogClient;
import com.bookstore.admin.client.OrderClient;
import com.bookstore.admin.dto.BookDto;
import com.bookstore.admin.dto.OrderDto;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardController Unit Tests")
class DashboardControllerTest {

    private static final String ISBN1 = "ISBN1";
    private static final String BOOK_1 = "Book 1";
    private static final String AUTHOR_1 = "Author 1";
    private static final String DESC = "Desc";
    private static final String FICTION = "Fiction";
    private static final String DASHBOARD = "dashboard";
    private static final String API_BOOKS = "/api/books";
    private static final String API_ORDERS = "/api/orders";
    private static final String ERROR = "error";

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private OrderClient orderClient;

    @Mock
    private Model model;

    @InjectMocks
    private DashboardController dashboardController;

    @Test
    @DisplayName("dashboard should load successfully with books and orders")
    void testDashboard() {
        List<BookDto> books = new ArrayList<>();
        books.add(new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION));

        List<OrderDto> orders = new ArrayList<>();
        orders.add(new OrderDto("1", 1L, "user@test.com", null, BigDecimal.valueOf(100), "PENDING", null));

        when(catalogClient.getAllBooks()).thenReturn(books);
        when(orderClient.getAllOrders()).thenReturn(orders);

        String viewName = dashboardController.dashboard(model);

        assertThat(viewName).isEqualTo(DASHBOARD);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    @DisplayName("dashboard should handle catalog service error")
    void testDashboardWithError() {
        when(catalogClient.getAllBooks()).thenThrow(new RuntimeException("Service unavailable"));

        String viewName = dashboardController.dashboard(model);

        assertThat(viewName).isEqualTo(DASHBOARD);
        verify(model).addAttribute(eq(ERROR), anyString());
    }

    @Test
    @DisplayName("dashboard should handle order service error")
    void dashboard_whenOrderServiceFails_shouldAddErrorToModel() {
        List<BookDto> books = List.of(
            new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION)
        );

        when(catalogClient.getAllBooks()).thenReturn(books);
        when(orderClient.getAllOrders()).thenThrow(new RuntimeException("Order service unavailable"));

        String viewName = dashboardController.dashboard(model);

        assertThat(viewName).isEqualTo(DASHBOARD);
        verify(model).addAttribute(eq(ERROR), anyString());
    }

    @Test
    @DisplayName("dashboard should handle FeignException from catalog service")
    void dashboard_whenCatalogFeignException_shouldAddErrorToModel() {
        Request request = Request.create(Request.HttpMethod.GET, API_BOOKS, new HashMap<>(), null, new RequestTemplate());
        FeignException exception = new FeignException.ServiceUnavailable("Catalog service unavailable", request, null, null);

        when(catalogClient.getAllBooks()).thenThrow(exception);

        String viewName = dashboardController.dashboard(model);

        assertThat(viewName).isEqualTo(DASHBOARD);
        verify(model).addAttribute(eq(ERROR), anyString());
    }

    @Test
    @DisplayName("dashboard should handle FeignException from order service")
    void dashboard_whenOrderFeignException_shouldAddErrorToModel() {
        List<BookDto> books = List.of(
            new BookDto(1L, ISBN1, BOOK_1, AUTHOR_1, DESC, BigDecimal.TEN, 10, FICTION)
        );
        Request request = Request.create(Request.HttpMethod.GET, API_ORDERS, new HashMap<>(), null, new RequestTemplate());
        FeignException exception = new FeignException.ServiceUnavailable("Order service unavailable", request, null, null);

        when(catalogClient.getAllBooks()).thenReturn(books);
        when(orderClient.getAllOrders()).thenThrow(exception);

        String viewName = dashboardController.dashboard(model);

        assertThat(viewName).isEqualTo(DASHBOARD);
        verify(model).addAttribute(eq(ERROR), anyString());
    }

    @Test
    @DisplayName("dashboard should handle empty books and orders")
    void dashboard_whenEmptyData_shouldLoadSuccessfully() {
        when(catalogClient.getAllBooks()).thenReturn(List.of());
        when(orderClient.getAllOrders()).thenReturn(List.of());

        String viewName = dashboardController.dashboard(model);

        assertThat(viewName).isEqualTo(DASHBOARD);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }
}
