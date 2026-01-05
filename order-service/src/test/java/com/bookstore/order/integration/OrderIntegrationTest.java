package com.bookstore.order.integration;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.dto.OrderDTO;
import com.bookstore.common.dto.OrderItemDTO;
import com.bookstore.order.client.CatalogClient;
import com.bookstore.order.document.Order;
import com.bookstore.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @MockBean
    private CatalogClient catalogClient;

    private BookDTO testBook;
    private OrderDTO testOrderDTO;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        testBook = BookDTO.builder()
                .id(1L)
                .isbn("9780134685991")
                .title("Effective Java")
                .author("Joshua Bloch")
                .price(new BigDecimal("45.99"))
                .stock(100)
                .category("Programming")
                .build();

        OrderItemDTO orderItemDTO = OrderItemDTO.builder()
                .bookId(1L)
                .quantity(2)
                .build();

        testOrderDTO = OrderDTO.builder()
                .userId(1L)
                .items(Arrays.asList(orderItemDTO))
                .build();
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
    }

    @Test
    void shouldCreateAndRetrieveOrderSuccessfully() throws Exception {
        when(catalogClient.getBookById(anyLong())).thenReturn(testBook);

        String response = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(91.98))
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDTO createdOrder = objectMapper.readValue(response, OrderDTO.class);

        mockMvc.perform(get("/api/v1/orders/" + createdOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdOrder.getId()))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void shouldGetUserOrdersSuccessfully() throws Exception {
        when(catalogClient.getBookById(anyLong())).thenReturn(testBook);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/orders/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() throws Exception {
        when(catalogClient.getBookById(anyLong())).thenReturn(testBook);

        String response = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDTO createdOrder = objectMapper.readValue(response, OrderDTO.class);

        mockMvc.perform(put("/api/v1/orders/" + createdOrder.getId() + "/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldDeleteOrderSuccessfully() throws Exception {
        when(catalogClient.getBookById(anyLong())).thenReturn(testBook);

        String response = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDTO createdOrder = objectMapper.readValue(response, OrderDTO.class);

        mockMvc.perform(delete("/api/v1/orders/" + createdOrder.getId()))
                .andExpect(status().isNoContent());

        List<Order> orders = orderRepository.findAll();
        assertEquals(0, orders.size());
    }

    @Test
    void shouldGetOrdersByStatusSuccessfully() throws Exception {
        when(catalogClient.getBookById(anyLong())).thenReturn(testBook);

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderDTO)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/orders/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void shouldReturnNotFoundForNonExistentOrder() throws Exception {
        mockMvc.perform(get("/api/v1/orders/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
