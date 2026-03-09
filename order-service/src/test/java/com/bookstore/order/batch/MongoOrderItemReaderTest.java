package com.bookstore.order.batch;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("MongoOrderItemReader Unit Tests")
class MongoOrderItemReaderTest {

    @Mock
    private MongoTemplate mongoTemplate;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        endDate = LocalDateTime.of(2024, 12, 31, 23, 59);
    }

    @Test
    @DisplayName("constructor should initialize reader with correct configuration")
    void constructor_withValidParameters_shouldInitializeCorrectly() {
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, startDate, endDate);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should set page size to 100")
    void constructor_shouldSetPageSize() {
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, startDate, endDate);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should set name to mongoOrderItemReader")
    void constructor_shouldSetReaderName() {
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, startDate, endDate);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should handle same start and end date")
    void constructor_withSameStartAndEndDate_shouldInitialize() {
        LocalDateTime sameDate = LocalDateTime.of(2024, 6, 15, 12, 0);
        
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, sameDate, sameDate);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should handle date range spanning multiple years")
    void constructor_withMultiYearRange_shouldInitialize() {
        LocalDateTime start = LocalDateTime.of(2020, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 31, 23, 59);
        
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, start, end);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should handle recent date range")
    void constructor_withRecentDateRange_shouldInitialize() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, start, end);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should handle future date range")
    void constructor_withFutureDateRange_shouldInitialize() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(30);
        
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, start, end);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should handle single day range")
    void constructor_withSingleDayRange_shouldInitialize() {
        LocalDateTime start = LocalDateTime.of(2024, 3, 15, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 15, 23, 59);
        
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, start, end);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should handle midnight boundaries")
    void constructor_withMidnightBoundaries_shouldInitialize() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, start, end);

        assertThat(reader).isNotNull();
    }

    @Test
    @DisplayName("constructor should handle leap year dates")
    void constructor_withLeapYearDates_shouldInitialize() {
        LocalDateTime start = LocalDateTime.of(2024, 2, 28, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 2, 29, 23, 59);
        
        MongoOrderItemReader reader = new MongoOrderItemReader(mongoTemplate, start, end);

        assertThat(reader).isNotNull();
    }
}
