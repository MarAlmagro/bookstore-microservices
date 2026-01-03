package com.bookstore.order.service;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.dto.OrderDTO;
import com.bookstore.common.dto.OrderItemDTO;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderDTO testOrderDTO;
    private BookDTO testBook;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderService, "catalogServiceUrl", "http://localhost:8081");

        testBook = BookDTO.builder()
                .id(1L)
                .isbn("9780134685991")
                .title("Effective Java")
                .author("Joshua Bloch")
                .price(new BigDecimal("45.99"))
                .stock(100)
                .category("Programming")
                .build();

        OrderItem orderItem = OrderItem.builder()
                .bookId(1L)
                .quantity(2)
                .price(new BigDecimal("45.99"))
                .bookTitle("Effective Java")
                .bookIsbn("9780134685991")
                .build();

        testOrder = Order.builder()
                .id("order123")
                .userId(1L)
                .items(Arrays.asList(orderItem))
                .totalAmount(new BigDecimal("91.98"))
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

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
    void shouldCreateOrderSuccessfully() {
        when(restTemplate.getForObject(anyString(), eq(BookDTO.class))).thenReturn(testBook);
        when(orderMapper.toDocument(any(OrderDTO.class))).thenReturn(testOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(testOrderDTO);

        OrderDTO result = orderService.createOrder(testOrderDTO);

        assertNotNull(result);
        assertEquals(testOrderDTO.getId(), result.getId());
        assertEquals(testOrderDTO.getUserId(), result.getUserId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingOrderWithInsufficientStock() {
        BookDTO bookWithLowStock = BookDTO.builder()
                .id(1L)
                .stock(1)
                .price(new BigDecimal("45.99"))
                .title("Effective Java")
                .build();

        when(restTemplate.getForObject(anyString(), eq(BookDTO.class))).thenReturn(bookWithLowStock);

        assertThrows(InvalidRequestException.class, () -> orderService.createOrder(testOrderDTO));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void shouldGetOrderByIdSuccessfully() {
        when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
        when(orderMapper.toDTO(testOrder)).thenReturn(testOrderDTO);

        OrderDTO result = orderService.getOrderById("order123");

        assertNotNull(result);
        assertEquals("order123", result.getId());
        verify(orderRepository, times(1)).findById("order123");
    }

    @Test
    void shouldThrowExceptionWhenOrderNotFound() {
        when(orderRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.getOrderById("invalid"));
    }

    @Test
    void shouldGetUserOrdersSuccessfully() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(orders);
        when(orderMapper.toDTOList(orders)).thenReturn(Arrays.asList(testOrderDTO));

        List<OrderDTO> result = orderService.getUserOrders(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void shouldGetOrdersByStatusSuccessfully() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);
        when(orderMapper.toDTOList(orders)).thenReturn(Arrays.asList(testOrderDTO));

        List<OrderDTO> result = orderService.getOrdersByStatus(OrderStatus.PENDING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findByStatus(OrderStatus.PENDING);
    }

    @Test
    void shouldUpdateOrderStatusSuccessfully() {
        when(orderRepository.findById("order123")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        when(orderMapper.toDTO(any(Order.class))).thenReturn(testOrderDTO);

        OrderDTO result = orderService.updateOrderStatus("order123", OrderStatus.CONFIRMED);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void shouldDeleteOrderSuccessfully() {
        when(orderRepository.existsById("order123")).thenReturn(true);
        doNothing().when(orderRepository).deleteById("order123");

        orderService.deleteOrder("order123");

        verify(orderRepository, times(1)).deleteById("order123");
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentOrder() {
        when(orderRepository.existsById("invalid")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder("invalid"));
        verify(orderRepository, never()).deleteById(anyString());
    }

    @Test
    void shouldGetAllOrdersSuccessfully() {
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toDTOList(orders)).thenReturn(Arrays.asList(testOrderDTO));

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void shouldValidateOrderOwnershipSuccessfully() {
        when(orderRepository.existsByIdAndUserId("order123", 1L)).thenReturn(true);

        boolean result = orderService.validateOrderOwnership("order123", 1L);

        assertTrue(result);
        verify(orderRepository, times(1)).existsByIdAndUserId("order123", 1L);
    }

    @Test
    void shouldReturnFalseForInvalidOrderOwnership() {
        when(orderRepository.existsByIdAndUserId("order123", 999L)).thenReturn(false);

        boolean result = orderService.validateOrderOwnership("order123", 999L);

        assertFalse(result);
    }
}
