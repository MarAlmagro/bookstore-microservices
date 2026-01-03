package com.bookstore.order.repository;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.order.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByStatus(OrderStatus status);

    boolean existsByIdAndUserId(String id, Long userId);
}
