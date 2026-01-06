package com.bookstore.order.controller;

import com.bookstore.common.constants.OrderStatus;
import com.bookstore.common.dto.OrderDto;
import com.bookstore.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management APIs")
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order", description = "Creates a new order and validates book availability")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid order data or insufficient stock"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderDto orderDto) {
        log.info("Received request to create order for user: {}", orderDto.getUserId());
        OrderDto createdOrder = orderService.createOrder(orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @Operation(summary = "Get order by ID", description = "Retrieves order details by order ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable String id) {
        log.info("Received request to get order: {}", id);
        OrderDto order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Get all orders", description = "Retrieves all orders in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.info("Received request to get all orders");
        List<OrderDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get user orders", description = "Retrieves all orders for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User orders retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDto>> getUserOrders(@PathVariable Long userId) {
        log.info("Received request to get orders for user: {}", userId);
        List<OrderDto> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get orders by status", description = "Retrieves all orders with a specific status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderDto>> getOrdersByStatus(@PathVariable OrderStatus status) {
        log.info("Received request to get orders with status: {}", status);
        List<OrderDto> orders = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Update order status", description = "Updates the status of an existing order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable String id,
            @RequestParam OrderStatus status) {
        log.info("Received request to update order {} status to: {}", id, status);
        OrderDto updatedOrder = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @Operation(summary = "Delete order", description = "Deletes an order by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable String id) {
        log.info("Received request to delete order: {}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
