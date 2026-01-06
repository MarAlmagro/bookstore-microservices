package com.bookstore.order.service;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.OrderDto;

import java.util.List;

public interface OrderService {

    OrderDto createOrder(OrderDto orderDto);

    OrderDto getOrderById(String id);

    List<OrderDto> getUserOrders(Long userId);

    List<OrderDto> getOrdersByStatus(OrderStatus status);

    OrderDto updateOrderStatus(String id, OrderStatus status);

    void deleteOrder(String id);

    List<OrderDto> getAllOrders();

    boolean validateOrderOwnership(String orderId, Long userId);
}
