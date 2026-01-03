package com.bookstore.order.controller;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.OrderDTO;
import com.bookstore.common.dto.OrderItemDTO;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.OverrideAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = OrderController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration.class,
                org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration.class
        })
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.bookstore.order.config.MongoAuditingConfig.class
))
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .bookId(1L)
                .quantity(2)
                .price(new BigDecimal("45.99"))
                .bookTitle("Effective Java")
                .bookIsbn("9780134685991")
                .build();

        testOrderDTO = OrderDTO.builder()
                .id("order123")
                .userId(1L)
                .items(Arrays.asList(orderItemDTO))
                .totalAmount(new BigDecimal("91.98"))
                .status(OrderStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateOrderSuccessfully() throws Exception {
        when(orderService.createOrder(any(OrderDTO.class))).thenReturn(testOrderDTO);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("order123"))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(orderService, times(1)).createOrder(any(OrderDTO.class));
    }

    @Test
    void shouldGetOrderByIdSuccessfully() throws Exception {
        when(orderService.getOrderById("order123")).thenReturn(testOrderDTO);

        mockMvc.perform(get("/api/v1/orders/order123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order123"))
                .andExpect(jsonPath("$.userId").value(1));

        verify(orderService, times(1)).getOrderById("order123");
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        when(orderService.getOrderById("invalid")).thenThrow(new ResourceNotFoundException("Order not found"));

        mockMvc.perform(get("/api/v1/orders/invalid"))
                .andExpect(status().isNotFound());

        verify(orderService, times(1)).getOrderById("invalid");
    }

    @Test
    void shouldGetAllOrdersSuccessfully() throws Exception {
        List<OrderDTO> orders = Arrays.asList(testOrderDTO);
        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("order123"));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void shouldGetUserOrdersSuccessfully() throws Exception {
        List<OrderDTO> orders = Arrays.asList(testOrderDTO);
        when(orderService.getUserOrders(1L)).thenReturn(orders);

        mockMvc.perform(get("/api/v1/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));

        verify(orderService, times(1)).getUserOrders(1L);
    }

    @Test
    void shouldGetOrdersByStatusSuccessfully() throws Exception {
        List<OrderDTO> orders = Arrays.asList(testOrderDTO);
        when(orderService.getOrdersByStatus(OrderStatus.PENDING)).thenReturn(orders);

        mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        verify(orderService, times(1)).getOrdersByStatus(OrderStatus.PENDING);
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() throws Exception {
        OrderDTO updatedOrder = OrderDTO.builder()
                .id("order123")
                .userId(1L)
                .items(testOrderDTO.getItems())
                .totalAmount(new BigDecimal("91.98"))
                .status(OrderStatus.CONFIRMED.name())
                .createdAt(LocalDateTime.now())
                .build();

        when(orderService.updateOrderStatus("order123", OrderStatus.CONFIRMED)).thenReturn(updatedOrder);

        mockMvc.perform(put("/api/v1/orders/order123/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(orderService, times(1)).updateOrderStatus("order123", OrderStatus.CONFIRMED);
    }

    @Test
    void shouldDeleteOrderSuccessfully() throws Exception {
        doNothing().when(orderService).deleteOrder("order123");

        mockMvc.perform(delete("/api/v1/orders/order123"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder("order123");
    }
}
