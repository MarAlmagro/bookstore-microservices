package com.bookstore.order.service;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.OrderDTO;

import java.util.List;

public interface OrderService {

    OrderDTO createOrder(OrderDTO orderDTO);

    OrderDTO getOrderById(String id);

    List<OrderDTO> getUserOrders(Long userId);

    List<OrderDTO> getOrdersByStatus(OrderStatus status);

    OrderDTO updateOrderStatus(String id, OrderStatus status);

    void deleteOrder(String id);

    List<OrderDTO> getAllOrders();

    boolean validateOrderOwnership(String orderId, Long userId);
}
