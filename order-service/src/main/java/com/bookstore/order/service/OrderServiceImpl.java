package com.bookstore.order.service;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.BookDto;
import com.bookstore.common.dto.OrderDto;
import com.bookstore.common.dto.OrderItemDto;
import com.bookstore.common.dto.PageRequestDto;
import com.bookstore.common.dto.PageResponseDto;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.common.util.PageMapper;
import com.bookstore.order.client.CatalogClient;
import com.bookstore.order.document.Order;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.repository.OrderRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_NOT_FOUND_LOG = "Order not found with id: {}";
    private static final String ORDER_NOT_FOUND_MSG = "Order not found with id: ";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CatalogClient catalogClient;

    @Override
    public OrderDto createOrder(OrderDto orderDto) {
        log.debug("Creating new order for user: {}", orderDto.getUserId());

        validateAndEnrichOrderItems(orderDto);

        BigDecimal totalAmount = calculateTotalAmount(orderDto.getItems());
        orderDto.setTotalAmount(totalAmount);
        orderDto.setStatus(OrderStatus.PENDING.name());

        Order order = orderMapper.toDocument(orderDto);
        Order savedOrder = orderRepository.save(order);

        log.info("Created order with id: {} for user: {}", savedOrder.getId(), savedOrder.getUserId());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public OrderDto getOrderById(String id) {
        log.debug("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ORDER_NOT_FOUND_LOG, id);
                    return new ResourceNotFoundException(ORDER_NOT_FOUND_MSG + id);
                });
        log.info("Found order: {}", order.getId());
        return orderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> getUserOrders(Long userId) {
        log.debug("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Found {} orders for user: {}", orders.size(), userId);
        return orderMapper.toDtoList(orders);
    }

    @Override
    public List<OrderDto> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatus(status);
        log.info("Found {} orders with status: {}", orders.size(), status);
        return orderMapper.toDtoList(orders);
    }

    @Override
    public OrderDto updateOrderStatus(String id, OrderStatus status) {
        log.debug("Updating order {} status to: {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(ORDER_NOT_FOUND_LOG, id);
                    return new ResourceNotFoundException(ORDER_NOT_FOUND_MSG + id);
                });

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Updated order {} status to: {}", id, status);
        return orderMapper.toDto(updatedOrder);
    }

    @Override
    public void deleteOrder(String id) {
        log.debug("Deleting order with id: {}", id);
        if (!orderRepository.existsById(id)) {
            log.error(ORDER_NOT_FOUND_LOG, id);
            throw new ResourceNotFoundException(ORDER_NOT_FOUND_MSG + id);
        }
        orderRepository.deleteById(id);
        log.info("Deleted order with id: {}", id);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        log.debug("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        log.info("Found {} total orders", orders.size());
        return orderMapper.toDtoList(orders);
    }

    @Override
    public boolean validateOrderOwnership(String orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    private void validateAndEnrichOrderItems(OrderDto orderDto) {
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new InvalidRequestException("Order must contain at least one item");
        }

        for (OrderItemDto item : orderDto.getItems()) {
            BookDto book = fetchBookFromCatalog(item.getBookId());

            if (book.getStock() < item.getQuantity()) {
                throw new InvalidRequestException(
                        String.format("Insufficient stock for book: %s. Available: %d, Requested: %d",
                                book.getTitle(), book.getStock(), item.getQuantity()));
            }

            item.setPrice(book.getPrice());
            item.setBookTitle(book.getTitle());
            item.setBookIsbn(book.getIsbn());
        }
    }

    @CircuitBreaker(name = "catalogService")
    @Retry(name = "catalogService")
    @Bulkhead(name = "catalogService")
    private BookDto fetchBookFromCatalog(Long bookId) {
        try {
            log.debug("Fetching book from catalog service with id: {}", bookId);

            BookDto book = catalogClient.getBookById(bookId);

            if (book == null) {
                throw new ResourceNotFoundException("Book not found with id: " + bookId);
            }

            return book;
        } catch (Exception e) {
            log.error("Error fetching book from catalog service: {}", e.getMessage());
            throw new InvalidRequestException("Unable to validate book with id: " + bookId + ". " + e.getMessage());
        }
    }

    private BigDecimal calculateTotalAmount(List<OrderItemDto> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public PageResponseDto<OrderDto> getAllOrdersPaginated(PageRequestDto pageRequest) {
        log.debug("Fetching paginated orders - page: {}, size: {}, sortBy: {}, sortDir: {}",
                pageRequest.getPage(), pageRequest.getSize(), pageRequest.getSortBy(), pageRequest.getSortDir());
        
        Pageable pageable = PageMapper.toPageable(pageRequest);
        Page<Order> orderPage = orderRepository.findAll(pageable);
        
        List<OrderDto> orderDtos = orderPage.getContent().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
        
        PageResponseDto<OrderDto> response = PageMapper.toPageResponse(orderPage, OrderDto.class);
        response.setContent(orderDtos);
        
        log.debug("Returning {} orders out of {} total", orderDtos.size(), orderPage.getTotalElements());
        return response;
    }

    @Override
    public PageResponseDto<OrderDto> getUserOrdersPaginated(Long userId, PageRequestDto pageRequest) {
        log.debug("Fetching paginated orders for user: {} - page: {}, size: {}",
                userId, pageRequest.getPage(), pageRequest.getSize());
        
        Pageable pageable = PageMapper.toPageable(pageRequest);
        Page<Order> orderPage = orderRepository.findByUserId(userId, pageable);
        
        List<OrderDto> orderDtos = orderPage.getContent().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
        
        PageResponseDto<OrderDto> response = PageMapper.toPageResponse(orderPage, OrderDto.class);
        response.setContent(orderDtos);
        
        log.debug("Returning {} orders for user {} out of {} total",
                orderDtos.size(), userId, orderPage.getTotalElements());
        return response;
    }
}
