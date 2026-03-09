package com.bookstore.admin.client;

import com.bookstore.admin.dto.OrderDto;
import com.bookstore.admin.dto.OrderItemDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderClient Integration Tests")
class OrderClientTest {

    private static final String BOOK_1 = "Book 1";
    private static final String BOOK_2 = "Book 2";
    private static final String ORDER_001 = "order-001";
    private static final String ORDER_002 = "order-002";
    private static final String USER1_EMAIL = "user1@example.com";
    private static final String USER2_EMAIL = "user2@example.com";
    private static final String PENDING = "PENDING";
    private static final String COMPLETED = "COMPLETED";
    private static final String SHIPPED = "SHIPPED";
    private static final String API_ORDERS = "/api/orders";
    private static final String API_ORDERS_ORDER_001 = "/api/orders/order-001";
    private static final String API_ORDERS_USER_1 = "/api/orders/user/1";
    private static final String API_ORDERS_USER_999 = "/api/orders/user/999";
    private static final String API_ORDERS_ORDER_001_STATUS = "/api/orders/order-001/status";
    private static final String API_ORDERS_NON_EXISTENT = "/api/orders/non-existent";
    private static final String API_ORDERS_NON_EXISTENT_STATUS = "/api/orders/non-existent/status";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String STATUS = "status";

    private OrderClient orderClient;
    private ObjectMapper objectMapper;
    private WireMockServer wireMockServer;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(wireMockConfig().port(8090));
        wireMockServer.start();
        WireMock.configureFor("localhost", 8090);
        
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        orderClient = Feign.builder()
                .contract(new SpringMvcContract())
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .target(OrderClient.class, "http://localhost:8090/api/orders");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @DisplayName("getAllOrders should return list of orders when successful")
    void getAllOrders_whenSuccessful_shouldReturnOrderList() throws Exception {
        OrderItemDto item1 = new OrderItemDto(1L, BOOK_1, 2, BigDecimal.valueOf(29.99));
        OrderItemDto item2 = new OrderItemDto(2L, BOOK_2, 1, BigDecimal.valueOf(39.99));
        
        OrderDto order1 = new OrderDto(ORDER_001, 1L, USER1_EMAIL, 
                                       LocalDateTime.of(2024, 1, 15, 10, 30),
                                       BigDecimal.valueOf(99.97), PENDING, List.of(item1, item2));
        OrderDto order2 = new OrderDto(ORDER_002, 2L, USER2_EMAIL, 
                                       LocalDateTime.of(2024, 1, 16, 14, 45),
                                       BigDecimal.valueOf(59.98), COMPLETED, List.of(item1));
        List<OrderDto> orders = List.of(order1, order2);

        stubFor(get(urlEqualTo(API_ORDERS))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(orders))));

        List<OrderDto> result = orderClient.getAllOrders();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(ORDER_001);
        assertThat(result.get(0).getStatus()).isEqualTo(PENDING);
        assertThat(result.get(1).getId()).isEqualTo(ORDER_002);
        assertThat(result.get(1).getStatus()).isEqualTo(COMPLETED);
        verify(getRequestedFor(urlEqualTo(API_ORDERS)));
    }

    @Test
    @DisplayName("getAllOrders should throw FeignException when server error occurs")
    void getAllOrders_whenServerError_shouldThrowFeignException() {
        stubFor(get(urlEqualTo(API_ORDERS))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        assertThatThrownBy(() -> orderClient.getAllOrders())
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("getOrderById should return order when found")
    void getOrderById_whenFound_shouldReturnOrder() throws Exception {
        OrderItemDto item = new OrderItemDto(1L, BOOK_1, 2, BigDecimal.valueOf(29.99));
        OrderDto order = new OrderDto(ORDER_001, 1L, USER1_EMAIL, 
                                      LocalDateTime.of(2024, 1, 15, 10, 30),
                                      BigDecimal.valueOf(59.98), PENDING, List.of(item));

        stubFor(get(urlEqualTo(API_ORDERS_ORDER_001))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(order))));

        OrderDto result = orderClient.getOrderById(ORDER_001);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ORDER_001);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(PENDING);
        assertThat(result.getItems()).hasSize(1);
        verify(getRequestedFor(urlEqualTo(API_ORDERS_ORDER_001)));
    }

    @Test
    @DisplayName("getOrderById should throw FeignException when not found")
    void getOrderById_whenNotFound_shouldThrowFeignException() {
        stubFor(get(urlEqualTo(API_ORDERS_NON_EXISTENT))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Order not found")));

        assertThatThrownBy(() -> orderClient.getOrderById("non-existent"))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("getOrdersByUserId should return user orders when found")
    void getOrdersByUserId_whenFound_shouldReturnUserOrders() throws Exception {
        OrderItemDto item1 = new OrderItemDto(1L, BOOK_1, 2, BigDecimal.valueOf(29.99));
        OrderItemDto item2 = new OrderItemDto(2L, BOOK_2, 1, BigDecimal.valueOf(39.99));
        
        OrderDto order1 = new OrderDto(ORDER_001, 1L, USER1_EMAIL, 
                                       LocalDateTime.of(2024, 1, 15, 10, 30),
                                       BigDecimal.valueOf(59.98), PENDING, List.of(item1));
        OrderDto order2 = new OrderDto(ORDER_002, 1L, USER1_EMAIL, 
                                       LocalDateTime.of(2024, 1, 16, 14, 45),
                                       BigDecimal.valueOf(39.99), COMPLETED, List.of(item2));
        List<OrderDto> orders = List.of(order1, order2);

        stubFor(get(urlEqualTo(API_ORDERS_USER_1))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(orders))));

        List<OrderDto> result = orderClient.getOrdersByUserId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(order -> order.getUserId().equals(1L));
        assertThat(result.get(0).getId()).isEqualTo(ORDER_001);
        assertThat(result.get(1).getId()).isEqualTo(ORDER_002);
        verify(getRequestedFor(urlEqualTo(API_ORDERS_USER_1)));
    }

    @Test
    @DisplayName("getOrdersByUserId should return empty list when user has no orders")
    void getOrdersByUserId_whenNoOrders_shouldReturnEmptyList() throws Exception {
        stubFor(get(urlEqualTo(API_ORDERS_USER_999))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(List.of()))));

        List<OrderDto> result = orderClient.getOrdersByUserId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("updateOrderStatus should return updated order when successful")
    void updateOrderStatus_whenSuccessful_shouldReturnUpdatedOrder() throws Exception {
        OrderItemDto item = new OrderItemDto(1L, BOOK_1, 2, BigDecimal.valueOf(29.99));
        OrderDto updatedOrder = new OrderDto(ORDER_001, 1L, USER1_EMAIL, 
                                             LocalDateTime.of(2024, 1, 15, 10, 30),
                                             BigDecimal.valueOf(59.98), SHIPPED, List.of(item));

        stubFor(put(urlPathEqualTo(API_ORDERS_ORDER_001_STATUS))
                .withQueryParam(STATUS, equalTo(SHIPPED))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(updatedOrder))));

        OrderDto result = orderClient.updateOrderStatus(ORDER_001, SHIPPED);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ORDER_001);
        assertThat(result.getStatus()).isEqualTo(SHIPPED);
        verify(putRequestedFor(urlPathEqualTo(API_ORDERS_ORDER_001_STATUS))
                .withQueryParam(STATUS, equalTo(SHIPPED)));
    }

    @Test
    @DisplayName("updateOrderStatus should throw FeignException when order not found")
    void updateOrderStatus_whenNotFound_shouldThrowFeignException() {
        stubFor(put(urlPathEqualTo(API_ORDERS_NON_EXISTENT_STATUS))
                .withQueryParam(STATUS, equalTo(SHIPPED))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("Order not found")));

        assertThatThrownBy(() -> orderClient.updateOrderStatus("non-existent", SHIPPED))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("updateOrderStatus should throw FeignException when invalid status provided")
    void updateOrderStatus_whenInvalidStatus_shouldThrowFeignException() {
        stubFor(put(urlPathEqualTo(API_ORDERS_ORDER_001_STATUS))
                .withQueryParam(STATUS, equalTo("INVALID_STATUS"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Invalid status")));

        assertThatThrownBy(() -> orderClient.updateOrderStatus(ORDER_001, "INVALID_STATUS"))
                .isInstanceOf(FeignException.class);
    }

    @Test
    @DisplayName("getAllOrders should handle empty response")
    void getAllOrders_whenEmpty_shouldReturnEmptyList() throws Exception {
        stubFor(get(urlEqualTo(API_ORDERS))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(objectMapper.writeValueAsString(List.of()))));

        List<OrderDto> result = orderClient.getAllOrders();

        assertThat(result).isEmpty();
    }
}
