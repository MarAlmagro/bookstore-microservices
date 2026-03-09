package com.bookstore.order.repository;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@TestPropertySource(properties = {
    "spring.mongodb.embedded.version=3.5.5"
})
@DisplayName("OrderRepository Integration Tests")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    private Order order1;
    private Order order2;
    private Order order3;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        OrderItem item1 = OrderItem.builder()
                .bookId(1L)
                .quantity(2)
                .price(new BigDecimal("29.99"))
                .bookTitle("Book 1")
                .bookIsbn("ISBN-001")
                .build();

        OrderItem item2 = OrderItem.builder()
                .bookId(2L)
                .quantity(1)
                .price(new BigDecimal("19.99"))
                .bookTitle("Book 2")
                .bookIsbn("ISBN-002")
                .build();

        order1 = Order.builder()
                .userId(100L)
                .items(Arrays.asList(item1))
                .totalAmount(new BigDecimal("59.98"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 0))
                .shippingAddress("123 Main St")
                .customerEmail("user1@example.com")
                .customerName("User One")
                .build();

        order2 = Order.builder()
                .userId(100L)
                .items(Arrays.asList(item2))
                .totalAmount(new BigDecimal("19.99"))
                .status(OrderStatus.CONFIRMED)
                .createdAt(LocalDateTime.of(2024, 1, 20, 14, 30))
                .shippingAddress("123 Main St")
                .customerEmail("user1@example.com")
                .customerName("User One")
                .build();

        order3 = Order.builder()
                .userId(200L)
                .items(Arrays.asList(item1, item2))
                .totalAmount(new BigDecimal("79.97"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 1, 25, 16, 45))
                .shippingAddress("456 Oak Ave")
                .customerEmail("user2@example.com")
                .customerName("User Two")
                .build();

        order1 = orderRepository.save(order1);
        order2 = orderRepository.save(order2);
        order3 = orderRepository.save(order3);
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc should return orders sorted by creation date descending")
    void findByUserIdOrderByCreatedAtDesc_withValidUserId_shouldReturnSortedOrders() {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(100L);

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getId()).isEqualTo(order2.getId());
        assertThat(orders.get(1).getId()).isEqualTo(order1.getId());
        assertThat(orders.get(0).getCreatedAt()).isAfter(orders.get(1).getCreatedAt());
    }

    @Test
    @DisplayName("findByUserIdOrderByCreatedAtDesc should return empty list when user has no orders")
    void findByUserIdOrderByCreatedAtDesc_withNoOrders_shouldReturnEmptyList() {
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(999L);

        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("findByStatus should return all orders with given status")
    void findByStatus_withValidStatus_shouldReturnMatchingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);

        assertThat(pendingOrders).hasSize(2);
        assertThat(pendingOrders).extracting(Order::getStatus)
                .containsOnly(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("findByStatus should return empty list when no orders have the status")
    void findByStatus_withNoMatchingStatus_shouldReturnEmptyList() {
        List<Order> shippedOrders = orderRepository.findByStatus(OrderStatus.SHIPPED);

        assertThat(shippedOrders).isEmpty();
    }

    @Test
    @DisplayName("findByStatus should return orders with CONFIRMED status")
    void findByStatus_withConfirmedStatus_shouldReturnConfirmedOrders() {
        List<Order> confirmedOrders = orderRepository.findByStatus(OrderStatus.CONFIRMED);

        assertThat(confirmedOrders).hasSize(1);
        assertThat(confirmedOrders.get(0).getId()).isEqualTo(order2.getId());
    }

    @Test
    @DisplayName("existsByIdAndUserId should return true when order exists for user")
    void existsByIdAndUserId_withValidIdAndUserId_shouldReturnTrue() {
        boolean exists = orderRepository.existsByIdAndUserId(order1.getId(), 100L);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByIdAndUserId should return false when order does not exist")
    void existsByIdAndUserId_withInvalidId_shouldReturnFalse() {
        boolean exists = orderRepository.existsByIdAndUserId("non-existent-id", 100L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("existsByIdAndUserId should return false when order exists but belongs to different user")
    void existsByIdAndUserId_withDifferentUserId_shouldReturnFalse() {
        boolean exists = orderRepository.existsByIdAndUserId(order1.getId(), 200L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findByUserId should return all orders for user")
    void findByUserId_withValidUserId_shouldReturnAllUserOrders() {
        List<Order> orders = orderRepository.findByUserId(100L);

        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getUserId)
                .containsOnly(100L);
    }

    @Test
    @DisplayName("findByUserIdAndStatus should return orders matching both criteria")
    void findByUserIdAndStatus_withValidParams_shouldReturnMatchingOrders() {
        List<Order> orders = orderRepository.findByUserIdAndStatus(100L, OrderStatus.PENDING);

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getId()).isEqualTo(order1.getId());
    }

    @Test
    @DisplayName("findByCreatedAtBetween should return orders within date range")
    void findByCreatedAtBetween_withValidRange_shouldReturnOrdersInRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 18, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 26, 0, 0);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        assertThat(orders).hasSize(2);
        assertThat(orders).extracting(Order::getId)
                .containsExactlyInAnyOrder(order2.getId(), order3.getId());
    }

    @Test
    @DisplayName("findByCreatedAtBetween should return empty list when no orders in range")
    void findByCreatedAtBetween_withNoOrdersInRange_shouldReturnEmptyList() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        assertThat(orders).isEmpty();
    }

    @Test
    @DisplayName("countByStatus should return correct count for status")
    void countByStatus_withValidStatus_shouldReturnCorrectCount() {
        long count = orderRepository.countByStatus(OrderStatus.PENDING);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countByStatus should return zero when no orders have status")
    void countByStatus_withNoMatchingStatus_shouldReturnZero() {
        long count = orderRepository.countByStatus(OrderStatus.DELIVERED);

        assertThat(count).isZero();
    }

    @Test
    @DisplayName("findByUserId with Pageable should return paginated results")
    void findByUserId_withPageable_shouldReturnPaginatedResults() {
        PageRequest pageRequest = PageRequest.of(0, 1);
        
        Page<Order> page = orderRepository.findByUserId(100L, pageRequest);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByUserId with Pageable should return second page correctly")
    void findByUserId_withSecondPage_shouldReturnCorrectPage() {
        PageRequest pageRequest = PageRequest.of(1, 1);
        
        Page<Order> page = orderRepository.findByUserId(100L, pageRequest);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.isLast()).isTrue();
    }

    @Test
    @DisplayName("findByUserId with Pageable should return empty page when page exceeds total")
    void findByUserId_withPageBeyondTotal_shouldReturnEmptyPage() {
        PageRequest pageRequest = PageRequest.of(5, 10);
        
        Page<Order> page = orderRepository.findByUserId(100L, pageRequest);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("save should persist order with all fields")
    void save_withValidOrder_shouldPersistAllFields() {
        Order newOrder = Order.builder()
                .userId(300L)
                .items(Arrays.asList(OrderItem.builder()
                        .bookId(3L)
                        .quantity(5)
                        .price(new BigDecimal("15.00"))
                        .build()))
                .totalAmount(new BigDecimal("75.00"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .shippingAddress("789 Pine Rd")
                .customerEmail("user3@example.com")
                .customerName("User Three")
                .build();

        Order saved = orderRepository.save(newOrder);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserId()).isEqualTo(300L);
        assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("75.00"));
        assertThat(saved.getShippingAddress()).isEqualTo("789 Pine Rd");
    }

    @Test
    @DisplayName("deleteAll should remove all orders")
    void deleteAll_shouldRemoveAllOrders() {
        orderRepository.deleteAll();

        long count = orderRepository.count();
        assertThat(count).isZero();
    }
}
