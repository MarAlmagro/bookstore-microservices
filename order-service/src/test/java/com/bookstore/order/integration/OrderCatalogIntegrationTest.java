package com.bookstore.order.integration;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.BookDto;
import com.bookstore.common.dto.OrderDto;
import com.bookstore.common.dto.OrderItemDto;
import com.bookstore.order.document.Order;
import com.bookstore.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureWireMock(port = 0)
class OrderCatalogIntegrationTest extends BaseMongoIntegrationTest {

    @Value("${api.paths.orders}")
    private String ordersApiPath;

    @Value("${api.paths.books}")
    private String booksApiPath;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        WireMock.reset();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldCreateOrderWhenBookExistsInCatalog() throws Exception {
        BookDto bookDto = createBookDto(1L, "Test Book", new BigDecimal("29.99"), 10);
        stubCatalogServiceGetBook(1L, bookDto);

        OrderDto orderDto = createOrderDto(1L, 2);
        String orderJson = objectMapper.writeValueAsString(orderDto);

        mockMvc.perform(post(ordersApiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].bookId").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldRejectOrderWhenBookNotFoundInCatalog() throws Exception {
        stubCatalogServiceBookNotFound(999L);

        OrderDto orderDto = createOrderDto(999L, 1);
        String orderJson = objectMapper.writeValueAsString(orderDto);

        mockMvc.perform(post(ordersApiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isNotFound());

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldRejectOrderWhenInsufficientStock() throws Exception {
        BookDto bookDto = createBookDto(1L, "Test Book", new BigDecimal("29.99"), 1);
        stubCatalogServiceGetBook(1L, bookDto);

        OrderDto orderDto = createOrderDto(1L, 5);
        String orderJson = objectMapper.writeValueAsString(orderDto);

        mockMvc.perform(post(ordersApiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isBadRequest());

        List<Order> orders = orderRepository.findAll();
        assertThat(orders).isEmpty();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void shouldHandleCatalogServiceTimeout() throws Exception {
        stubCatalogServiceTimeout(1L);

        OrderDto orderDto = createOrderDto(1L, 1);
        String orderJson = objectMapper.writeValueAsString(orderDto);

        mockMvc.perform(post(ordersApiPath)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().is5xxServerError());
    }

    private void stubCatalogServiceGetBook(Long bookId, BookDto bookDto) throws Exception {
        stubFor(get(urlEqualTo(booksApiPath + bookId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(bookDto))));
    }

    private void stubCatalogServiceBookNotFound(Long bookId) {
        stubFor(get(urlEqualTo(booksApiPath + bookId))
                .willReturn(aResponse()
                        .withStatus(404)));
    }

    private void stubCatalogServiceTimeout(Long bookId) {
        stubFor(get(urlEqualTo(booksApiPath + bookId))
                .willReturn(aResponse()
                        .withFixedDelay(5000)
                        .withStatus(200)));
    }

    private BookDto createBookDto(Long id, String title, BigDecimal price, Integer stock) {
        BookDto dto = new BookDto();
        dto.setId(id);
        dto.setTitle(title);
        dto.setPrice(price);
        dto.setStock(stock);
        return dto;
    }

    private OrderDto createOrderDto(Long bookId, Integer quantity) {
        OrderDto orderDto = new OrderDto();
        orderDto.setUserId(1L);
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setBookId(bookId);
        itemDto.setQuantity(quantity);
        orderDto.setItems(List.of(itemDto));
        return orderDto;
    }
}
