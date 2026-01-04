package com.bookstore.order.service;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.dto.OrderDTO;
import com.bookstore.common.dto.OrderItemDTO;
import com.bookstore.common.exception.InvalidRequestException;
import com.bookstore.common.exception.ResourceNotFoundException;
import com.bookstore.order.client.CatalogClient;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import com.bookstore.order.mapper.OrderMapper;
import com.bookstore.order.repository.OrderRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final CatalogClient catalogClient;

    @Override
    public OrderDTO createOrder(OrderDTO orderDTO) {
        log.debug("Creating new order for user: {}", orderDTO.getUserId());

        validateAndEnrichOrderItems(orderDTO);

        BigDecimal totalAmount = calculateTotalAmount(orderDTO.getItems());
        orderDTO.setTotalAmount(totalAmount);
        orderDTO.setStatus(OrderStatus.PENDING.name());

        Order order = orderMapper.toDocument(orderDTO);
        Order savedOrder = orderRepository.save(order);

        log.info("Created order with id: {} for user: {}", savedOrder.getId(), savedOrder.getUserId());
        return orderMapper.toDTO(savedOrder);
    }

    @Override
    public OrderDTO getOrderById(String id) {
        log.debug("Fetching order with id: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });
        log.info("Found order: {}", order.getId());
        return orderMapper.toDTO(order);
    }

    @Override
    public List<OrderDTO> getUserOrders(Long userId) {
        log.debug("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.info("Found {} orders for user: {}", orders.size(), userId);
        return orderMapper.toDTOList(orders);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(OrderStatus status) {
        log.debug("Fetching orders with status: {}", status);
        List<Order> orders = orderRepository.findByStatus(status);
        log.info("Found {} orders with status: {}", orders.size(), status);
        return orderMapper.toDTOList(orders);
    }

    @Override
    public OrderDTO updateOrderStatus(String id, OrderStatus status) {
        log.debug("Updating order {} status to: {}", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", id);
                    return new ResourceNotFoundException("Order not found with id: " + id);
                });

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Updated order {} status to: {}", id, status);
        return orderMapper.toDTO(updatedOrder);
    }

    @Override
    public void deleteOrder(String id) {
        log.debug("Deleting order with id: {}", id);
        if (!orderRepository.existsById(id)) {
            log.error("Order not found with id: {}", id);
            throw new ResourceNotFoundException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
        log.info("Deleted order with id: {}", id);
    }

    @Override
    public List<OrderDTO> getAllOrders() {
        log.debug("Fetching all orders");
        List<Order> orders = orderRepository.findAll();
        log.info("Found {} total orders", orders.size());
        return orderMapper.toDTOList(orders);
    }

    @Override
    public boolean validateOrderOwnership(String orderId, Long userId) {
        return orderRepository.existsByIdAndUserId(orderId, userId);
    }

    private void validateAndEnrichOrderItems(OrderDTO orderDTO) {
        if (orderDTO.getItems() == null || orderDTO.getItems().isEmpty()) {
            throw new InvalidRequestException("Order must contain at least one item");
        }

        for (OrderItemDTO item : orderDTO.getItems()) {
            BookDTO book = fetchBookFromCatalog(item.getBookId());
            
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
    private BookDTO fetchBookFromCatalog(Long bookId) {
        try {
            log.debug("Fetching book from catalog service with id: {}", bookId);
            
            BookDTO book = catalogClient.getBookById(bookId);
            
            if (book == null) {
                throw new ResourceNotFoundException("Book not found with id: " + bookId);
            }
            
            return book;
        } catch (Exception e) {
            log.error("Error fetching book from catalog service: {}", e.getMessage());
            throw new InvalidRequestException("Unable to validate book with id: " + bookId + ". " + e.getMessage());
        }
    }

    private BigDecimal calculateTotalAmount(List<OrderItemDTO> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
