package com.bookstore.order.batch;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrderSkipListener Unit Tests")
class OrderSkipListenerTest {

    private OrderSkipListener listener;
    private static final String REJECTED_DIR = "exports/rejected";
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String PROCESSING_ERROR_MESSAGE = "Processing error";
    private static final String REJECTED_ORDERS_FILE_PATTERN = "rejected_orders_%s.log";

    @BeforeEach
    void setUp() {
        listener = new OrderSkipListener();
    }

    @AfterEach
    void tearDown() throws IOException {
        Path rejectedDirPath = Path.of(REJECTED_DIR);
        if (Files.exists(rejectedDirPath)) {
            try (var stream = Files.list(rejectedDirPath)) {
                stream.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        // Log and continue
                    }
                });
            }
            Files.deleteIfExists(rejectedDirPath);
        }
    }

    @Test
    @DisplayName("onSkipInRead should log warning without throwing exception")
    void onSkipInRead_withException_shouldLogWarning() {
        Throwable throwable = new RuntimeException("Read error");

        listener.onSkipInRead(throwable);

        assertThat(throwable).isNotNull();
    }

    @Test
    @DisplayName("onSkipInRead should handle null throwable message")
    void onSkipInRead_withNullMessage_shouldHandleGracefully() {
        Throwable throwable = new RuntimeException();

        listener.onSkipInRead(throwable);

        assertThat(throwable).isNotNull();
    }

    @Test
    @DisplayName("onSkipInWrite should log warning without throwing exception")
    void onSkipInWrite_withException_shouldLogWarning() {
        Object item = "test-item";
        Throwable throwable = new RuntimeException("Write error");

        listener.onSkipInWrite(item, throwable);

        assertThat(throwable).isNotNull();
    }

    @Test
    @DisplayName("onSkipInWrite should handle null item")
    void onSkipInWrite_withNullItem_shouldHandleGracefully() {
        Throwable throwable = new RuntimeException("Write error");

        listener.onSkipInWrite(null, throwable);

        assertThat(throwable).isNotNull();
    }

    @Test
    @DisplayName("onSkipInProcess should log warning and write to dead letter file")
    void onSkipInProcess_withValidOrder_shouldWriteToFile() throws IOException {
        Order order = Order.builder()
                .id("order-123")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(new BigDecimal("10.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        Throwable throwable = new RuntimeException(PROCESSING_ERROR_MESSAGE);

        listener.onSkipInProcess(order, throwable);

        File rejectedDir = new File(REJECTED_DIR);
        assertThat(rejectedDir).exists();
        assertThat(rejectedDir.isDirectory()).isTrue();

        String expectedFileName = String.format(REJECTED_ORDERS_FILE_PATTERN, LocalDateTime.now().format(FILE_DATE_FORMATTER));
        File deadLetterFile = new File(rejectedDir, expectedFileName);
        assertThat(deadLetterFile).exists();

        List<String> lines = Files.readAllLines(deadLetterFile.toPath());
        assertThat(lines).isNotEmpty();
        assertThat(lines.get(0)).contains("order-123");
        assertThat(lines.get(0)).contains("RuntimeException");
        assertThat(lines.get(0)).contains(PROCESSING_ERROR_MESSAGE);
    }

    @Test
    @DisplayName("onSkipInProcess should append to existing file")
    void onSkipInProcess_withMultipleOrders_shouldAppendToFile() throws IOException {
        Order order1 = Order.builder()
                .id("order-1")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .id("order-2")
                .userId(200L)
                .items(Arrays.asList(OrderItem.builder().bookId(2L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Throwable throwable1 = new RuntimeException("Error 1");
        Throwable throwable2 = new RuntimeException("Error 2");

        listener.onSkipInProcess(order1, throwable1);
        listener.onSkipInProcess(order2, throwable2);

        String expectedFileName = String.format(REJECTED_ORDERS_FILE_PATTERN, LocalDateTime.now().format(FILE_DATE_FORMATTER));
        File deadLetterFile = new File(REJECTED_DIR, expectedFileName);
        
        List<String> lines = Files.readAllLines(deadLetterFile.toPath());
        assertThat(lines).hasSize(2);
        assertThat(lines.get(0)).contains("order-1");
        assertThat(lines.get(1)).contains("order-2");
    }

    @Test
    @DisplayName("onSkipInProcess should handle null order gracefully")
    void onSkipInProcess_withNullOrder_shouldNotWriteFile() {
        Throwable throwable = new RuntimeException(PROCESSING_ERROR_MESSAGE);

        listener.onSkipInProcess(null, throwable);

        File rejectedDir = new File(REJECTED_DIR);
        assertThat(rejectedDir.exists()).isFalse();
    }

    @Test
    @DisplayName("onSkipInProcess should handle different exception types")
    void onSkipInProcess_withDifferentExceptions_shouldLogCorrectly() throws IOException {
        Order order = Order.builder()
                .id("order-exception-test")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Throwable throwable = new IllegalArgumentException("Invalid argument");

        listener.onSkipInProcess(order, throwable);

        String expectedFileName = String.format(REJECTED_ORDERS_FILE_PATTERN, LocalDateTime.now().format(FILE_DATE_FORMATTER));
        File deadLetterFile = new File(REJECTED_DIR, expectedFileName);
        
        List<String> lines = Files.readAllLines(deadLetterFile.toPath());
        assertThat(lines.get(0)).contains("IllegalArgumentException");
        assertThat(lines.get(0)).contains("Invalid argument");
    }

    @Test
    @DisplayName("onSkipInProcess should include timestamp in log entry")
    void onSkipInProcess_shouldIncludeTimestamp() throws IOException {
        Order order = Order.builder()
                .id("order-timestamp-test")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Throwable throwable = new RuntimeException("Test error");

        listener.onSkipInProcess(order, throwable);

        String expectedFileName = String.format(REJECTED_ORDERS_FILE_PATTERN, LocalDateTime.now().format(FILE_DATE_FORMATTER));
        File deadLetterFile = new File(REJECTED_DIR, expectedFileName);
        
        List<String> lines = Files.readAllLines(deadLetterFile.toPath());
        assertThat(lines.get(0)).matches(".*\\[\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*\\].*");
    }

    @Test
    @DisplayName("onSkipInProcess should handle order with null ID")
    void onSkipInProcess_withNullOrderId_shouldWriteUnknown() throws IOException {
        Order order = Order.builder()
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Throwable throwable = new RuntimeException("Test error");

        listener.onSkipInProcess(order, throwable);

        String expectedFileName = String.format(REJECTED_ORDERS_FILE_PATTERN, LocalDateTime.now().format(FILE_DATE_FORMATTER));
        File deadLetterFile = new File(REJECTED_DIR, expectedFileName);
        
        List<String> lines = Files.readAllLines(deadLetterFile.toPath());
        assertThat(lines.get(0)).contains("OrderId: null");
    }

    @Test
    @DisplayName("onSkipInProcess should handle exception with null message")
    void onSkipInProcess_withNullExceptionMessage_shouldHandleGracefully() throws IOException {
        Order order = Order.builder()
                .id("order-null-msg")
                .userId(100L)
                .items(Arrays.asList(OrderItem.builder().bookId(1L).quantity(1).price(BigDecimal.TEN).build()))
                .totalAmount(BigDecimal.TEN)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Throwable throwable = new RuntimeException();

        listener.onSkipInProcess(order, throwable);

        String expectedFileName = String.format(REJECTED_ORDERS_FILE_PATTERN, LocalDateTime.now().format(FILE_DATE_FORMATTER));
        File deadLetterFile = new File(REJECTED_DIR, expectedFileName);
        
        assertThat(deadLetterFile).exists();
    }
}
