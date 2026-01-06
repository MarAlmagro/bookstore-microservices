package com.bookstore.admin.client;

import com.bookstore.admin.dto.OrderDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "order-client", url = "http://localhost:8080", path = "/api/orders")
public interface OrderClient {

    @GetMapping
    List<OrderDto> getAllOrders();

    @GetMapping("/{id}")
    OrderDto getOrderById(@PathVariable("id") String id);

    @GetMapping("/user/{userId}")
    List<OrderDto> getOrdersByUserId(@PathVariable("userId") Long userId);

    @PutMapping("/{id}/status")
    OrderDto updateOrderStatus(@PathVariable("id") String id, @RequestParam("status") String status);
}
