package com.bookstore.order.batch;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.OrderReportDto;
import com.bookstore.common.exception.MalformedDataException;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MainframeOrderProcessor Unit Tests")
class MainframeOrderProcessorTest {

    private MainframeOrderProcessor processor;
    private static final String FAILED_TO_PROCESS_ORDER_MESSAGE = "Failed to process order";

    @BeforeEach
    void setUp() {
        processor = new MainframeOrderProcessor();
    }

    @Test
    @DisplayName("process should transform Order to OrderReportDto with all fields")
    void process_withValidOrder_shouldTransformCorrectly() throws Exception {
        OrderItem item1 = OrderItem.builder()
                .bookId(1L)
                .quantity(2)
                .price(new BigDecimal("29.99"))
                .bookTitle("Test Book")
                .bookIsbn("ISBN-001")
                .build();

        Order order = Order.builder()
                .id("order-123")
                .userId(100L)
                .items(Arrays.asList(item1))
                .totalAmount(new BigDecimal("59.98"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 3, 15, 10, 30))
                .build();

        OrderReportDto result = processor.process(order);

        assertThat(result).isNotNull();
        assertThat(result.getNumericId()).isEqualTo(1L);
        assertThat(result.getCategory()).isEqualTo("GENERAL");
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("59.98"));
        assertThat(result.getFormattedDate()).isEqualTo("20240315");
    }

    @Test
    @DisplayName("process should increment sequence counter for each order")
    void process_withMultipleOrders_shouldIncrementSequence() throws Exception {
        Order order1 = Order.builder()
                .id("order-1")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        Order order2 = Order.builder()
                .id("order-2")
                .userId(200L)
                .items(Arrays.asList(OrderItem.builder().bookId(2L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 1, 2, 12, 0))
                .build();

        OrderReportDto result1 = processor.process(order1);
        OrderReportDto result2 = processor.process(order2);

        assertThat(result1.getNumericId()).isEqualTo(1L);
        assertThat(result2.getNumericId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("process should format date correctly")
    void process_withDifferentDates_shouldFormatCorrectly() throws Exception {
        Order order = Order.builder()
                .id("order-date-test")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 12, 31, 23, 59))
                .build();

        OrderReportDto result = processor.process(order);

        assertThat(result.getFormattedDate()).isEqualTo("20241231");
    }

    @Test
    @DisplayName("process should extract GENERAL category when items exist")
    void process_withItems_shouldExtractGeneralCategory() throws Exception {
        OrderItem item1 = OrderItem.builder()
                .bookId(1L)
                .quantity(1)
                .price(BigDecimal.TEN)
                .build();

        OrderItem item2 = OrderItem.builder()
                .bookId(2L)
                .quantity(2)
                .price(BigDecimal.TEN)
                .build();

        Order order = Order.builder()
                .id("order-category")
                .userId(100L)
                .items(Arrays.asList(item1, item2))
                .totalAmount(new BigDecimal("30.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        OrderReportDto result = processor.process(order);

        assertThat(result.getCategory()).isEqualTo("GENERAL");
    }

    @Test
    @DisplayName("process should extract UNKNOWN category when items are empty")
    void process_withEmptyItems_shouldExtractUnknownCategory() throws Exception {
        Order order = Order.builder()
                .id("order-no-items")
                .userId(100L)
                .items(Collections.emptyList())
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        OrderReportDto result = processor.process(order);

        assertThat(result.getCategory()).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("process should extract UNKNOWN category when items are null")
    void process_withNullItems_shouldExtractUnknownCategory() throws Exception {
        Order order = Order.builder()
                .id("order-null-items")
                .userId(100L)
                .totalAmount(BigDecimal.ZERO)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        OrderReportDto result = processor.process(order);

        assertThat(result.getCategory()).isEqualTo("UNKNOWN");
    }

    @Test
    @DisplayName("process should throw MalformedDataException when order ID is null")
    void process_withNullId_shouldThrowException() {
        Order order = Order.builder()
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> processor.process(order))
                .isInstanceOf(MalformedDataException.class)
                .hasMessageContaining(FAILED_TO_PROCESS_ORDER_MESSAGE);
    }

    @Test
    @DisplayName("process should throw MalformedDataException when totalAmount is null")
    void process_withNullTotalAmount_shouldThrowException() {
        Order order = Order.builder()
                .id("order-no-total")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> processor.process(order))
                .isInstanceOf(MalformedDataException.class)
                .hasMessageContaining(FAILED_TO_PROCESS_ORDER_MESSAGE);
    }

    @Test
    @DisplayName("process should throw MalformedDataException when createdAt is null")
    void process_withNullCreatedAt_shouldThrowException() {
        Order order = Order.builder()
                .id("order-no-date")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .build();

        assertThatThrownBy(() -> processor.process(order))
                .isInstanceOf(MalformedDataException.class)
                .hasMessageContaining(FAILED_TO_PROCESS_ORDER_MESSAGE);
    }

    @Test
    @DisplayName("process should handle large total amounts")
    void process_withLargeTotalAmount_shouldProcessCorrectly() throws Exception {
        Order order = Order.builder()
                .id("order-large-amount")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(100).price(new BigDecimal("999.99")).build()))
                .totalAmount(new BigDecimal("99999.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        OrderReportDto result = processor.process(order);

        assertThat(result).isNotNull();
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("99999.00"));
    }
}
