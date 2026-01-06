package com.bookstore.admin.controller;

import com.bookstore.admin.client.CatalogClient;
import com.bookstore.admin.client.OrderClient;
import com.bookstore.admin.dto.BookDto;
import com.bookstore.admin.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DashboardControllerTest {

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private OrderClient orderClient;

    @Mock
    private Model model;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testDashboard() {
        List<BookDto> books = new ArrayList<>();
        books.add(new BookDto(1L, "ISBN1", "Book 1", "Author 1", "Desc", BigDecimal.TEN, 10, "Fiction"));

        List<OrderDto> orders = new ArrayList<>();
        orders.add(new OrderDto("1", 1L, "user@test.com", null, BigDecimal.valueOf(100), "PENDING", null));

        when(catalogClient.getAllBooks()).thenReturn(books);
        when(orderClient.getAllOrders()).thenReturn(orders);

        String viewName = dashboardController.dashboard(model);

        assertEquals("dashboard", viewName);
        verify(model, atLeastOnce()).addAttribute(anyString(), any());
    }

    @Test
    void testDashboardWithError() {
        when(catalogClient.getAllBooks()).thenThrow(new RuntimeException("Service unavailable"));

        String viewName = dashboardController.dashboard(model);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute(eq("error"), anyString());
    }
}
