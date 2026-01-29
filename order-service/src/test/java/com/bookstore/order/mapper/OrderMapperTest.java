package com.bookstore.order.mapper;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.OrderDto;
import com.bookstore.common.dto.OrderItemDto;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderMapper Unit Tests")
class OrderMapperTest {

    private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        orderMapper = new OrderMapper();
    }

    @Test
    @DisplayName("toDto should map Order to OrderDto with all fields")
    void toDto_withValidOrder_shouldMapAllFields() {
        OrderItem item1 = OrderItem.builder()
                .bookId(1L)
                .quantity(2)
                .price(new BigDecimal("29.99"))
                .bookTitle("Test Book 1")
                .bookIsbn("ISBN-001")
                .build();

        OrderItem item2 = OrderItem.builder()
                .bookId(2L)
                .quantity(1)
                .price(new BigDecimal("19.99"))
                .bookTitle("Test Book 2")
                .bookIsbn("ISBN-002")
                .build();

        Order order = Order.builder()
                .id("order-123")
                .userId(100L)
                .items(Arrays.asList(item1, item2))
                .totalAmount(new BigDecimal("79.97"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30))
                .shippingAddress("123 Main St")
                .customerEmail("test@example.com")
                .customerName("John Doe")
                .build();

        OrderDto result = orderMapper.toDto(order);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("order-123");
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("79.97"));
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING.name());
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getBookId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
        assertThat(result.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("29.99"));
        assertThat(result.getItems().get(1).getBookId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toDto should return null when Order is null")
    void toDto_withNullOrder_shouldReturnNull() {
        OrderDto result = orderMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDto should handle Order with empty items list")
    void toDto_withEmptyItems_shouldMapCorrectly() {
        Order order = Order.builder()
                .id("order-456")
                .userId(200L)
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        OrderDto result = orderMapper.toDto(order);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("toDto should handle Order with null items")
    void toDto_withNullItems_shouldHandleGracefully() {
        Order order = Order.builder()
                .id("order-789")
                .userId(300L)
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        OrderDto result = orderMapper.toDto(order);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("toDocument should map OrderDto to Order with all fields")
    void toDocument_withValidDto_shouldMapAllFields() {
        OrderItemDto itemDto1 = OrderItemDto.builder()
                .bookId(1L)
                .quantity(3)
                .price(new BigDecimal("15.50"))
                .bookTitle("Book A")
                .bookIsbn("ISBN-A")
                .build();

        OrderItemDto itemDto2 = OrderItemDto.builder()
                .bookId(2L)
                .quantity(1)
                .price(new BigDecimal("25.00"))
                .bookTitle("Book B")
                .bookIsbn("ISBN-B")
                .build();

        OrderDto dto = OrderDto.builder()
                .id("dto-order-123")
                .userId(500L)
                .items(Arrays.asList(itemDto1, itemDto2))
                .totalAmount(new BigDecimal("71.50"))
                .status(OrderStatus.CONFIRMED.name())
                .createdAt(LocalDateTime.of(2024, 2, 20, 14, 45))
                .build();

        Order result = orderMapper.toDocument(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("dto-order-123");
        assertThat(result.getUserId()).isEqualTo(500L);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("71.50"));
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 2, 20, 14, 45));
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getBookId()).isEqualTo(1L);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(result.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(result.getItems().get(1).getBookId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("toDocument should return null when OrderDto is null")
    void toDocument_withNullDto_shouldReturnNull() {
        Order result = orderMapper.toDocument(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDocument should handle OrderDto with empty items")
    void toDocument_withEmptyItems_shouldMapCorrectly() {
        OrderDto dto = OrderDto.builder()
                .id("dto-order-456")
                .userId(600L)
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING.name())
                .createdAt(LocalDateTime.now())
                .build();

        Order result = orderMapper.toDocument(dto);

        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    @DisplayName("toItemDto should map OrderItem to OrderItemDto")
    void toItemDto_withValidItem_shouldMapCorrectly() {
        OrderItem item = OrderItem.builder()
                .bookId(10L)
                .quantity(5)
                .price(new BigDecimal("12.99"))
                .bookTitle("Test Item")
                .bookIsbn("ISBN-TEST")
                .build();

        OrderItemDto result = orderMapper.toItemDto(item);

        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isEqualTo(10L);
        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("12.99"));
        assertThat(result.getBookTitle()).isEqualTo("Test Item");
        assertThat(result.getBookIsbn()).isEqualTo("ISBN-TEST");
    }

    @Test
    @DisplayName("toItemDocument should map OrderItemDto to OrderItem")
    void toItemDocument_withValidDto_shouldMapCorrectly() {
        OrderItemDto dto = OrderItemDto.builder()
                .bookId(20L)
                .quantity(3)
                .price(new BigDecimal("8.75"))
                .bookTitle("Item DTO")
                .bookIsbn("ISBN-DTO")
                .build();

        OrderItem result = orderMapper.toItemDocument(dto);

        assertThat(result).isNotNull();
        assertThat(result.getBookId()).isEqualTo(20L);
        assertThat(result.getQuantity()).isEqualTo(3);
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("8.75"));
        assertThat(result.getBookTitle()).isEqualTo("Item DTO");
        assertThat(result.getBookIsbn()).isEqualTo("ISBN-DTO");
    }

    @Test
    @DisplayName("toDtoList should convert list of Orders to list of OrderDtos")
    void toDtoList_withValidList_shouldConvertAll() {
        Order order1 = Order.builder()
                .id("order-1")
                .userId(100L)
                .items(new ArrayList<>())
                .totalAmount(new BigDecimal("50.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .id("order-2")
                .userId(200L)
                .items(new ArrayList<>())
                .totalAmount(new BigDecimal("75.00"))
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .build();

        List<Order> orders = Arrays.asList(order1, order2);

        List<OrderDto> result = orderMapper.toDtoList(orders);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo("order-1");
        assertThat(result.get(0).getUserId()).isEqualTo(100L);
        assertThat(result.get(1).getId()).isEqualTo("order-2");
        assertThat(result.get(1).getUserId()).isEqualTo(200L);
    }

    @Test
    @DisplayName("toDtoList should return empty list when input is empty")
    void toDtoList_withEmptyList_shouldReturnEmptyList() {
        List<OrderDto> result = orderMapper.toDtoList(Collections.emptyList());

        assertThat(result).isEmpty();
    }
}
