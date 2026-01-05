package com.bookstore.order.batch;

import com.bookstore.common.dto.OrderReportDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MainframeOrderLineAggregatorTest {

    private MainframeOrderLineAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new MainframeOrderLineAggregator();
    }

    @Test
    void aggregate_ShouldProduce47Characters_WithTypicalInput() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(123456789L)
                .category("Fiction")
                .totalAmount(new BigDecimal("1234.56"))
                .formattedDate("20260105")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertEquals("010123456789Fiction        00000012345620260105", result);
    }

    @Test
    void aggregate_ShouldProduce47Characters_WithMinimalValues() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(1L)
                .category("A")
                .totalAmount(new BigDecimal("0.01"))
                .formattedDate("20200101")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertEquals("010000000001A              00000000000120200101", result);
    }

    @Test
    void aggregate_ShouldProduce47Characters_WithMaxValues() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(9999999999L)
                .category("VeryLongCategoryName")
                .totalAmount(new BigDecimal("99999999.99"))
                .formattedDate("20991231")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertEquals("019999999999VeryLongCategor00999999999920991231", result);
    }

    @Test
    void aggregate_ShouldProduce47Characters_WithZeroAmount() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(555L)
                .category("NonFiction")
                .totalAmount(BigDecimal.ZERO)
                .formattedDate("20260101")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertEquals("010000000555NonFiction     00000000000020260101", result);
    }

    @Test
    void aggregate_ShouldProduce47Characters_WithDecimalRounding() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(999L)
                .category("Science")
                .totalAmount(new BigDecimal("123.456"))
                .formattedDate("20260105")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertEquals("010000000999Science        00000001234620260105", result);
    }

    @Test
    void aggregate_ShouldTruncateCategory_WhenTooLong() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(100L)
                .category("ThisIsAVeryLongCategoryNameThatExceeds15Characters")
                .totalAmount(new BigDecimal("50.00"))
                .formattedDate("20260105")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertTrue(result.contains("ThisIsAVeryLong"), "Category should be truncated to 15 chars");
    }

    @Test
    void aggregate_ShouldPadCategory_WhenTooShort() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(200L)
                .category("Tech")
                .totalAmount(new BigDecimal("100.00"))
                .formattedDate("20260105")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertTrue(result.contains("Tech           "), "Category should be padded to 15 chars");
    }

    @Test
    void aggregate_ShouldFormatCorrectly_WithAllFieldsPresent() {
        OrderReportDTO order = OrderReportDTO.builder()
                .numericId(42L)
                .category("Mystery")
                .totalAmount(new BigDecimal("99.99"))
                .formattedDate("20260105")
                .build();

        String result = aggregator.aggregate(order);

        assertEquals(47, result.length(), "Output should be exactly 47 characters");
        assertEquals("01", result.substring(0, 2), "Should start with record type '01'");
        assertEquals("0000000042", result.substring(2, 12), "Numeric ID should be 10 digits");
        assertEquals("Mystery        ", result.substring(12, 27), "Category should be 15 chars");
        assertEquals("000000009999", result.substring(27, 39), "Amount should be 12 digits (in cents)");
        assertEquals("20260105", result.substring(39, 47), "Date should be 8 chars");
    }
}
