package com.bookstore.order.mapper;

import com.bookstore.common.dto.OrderDto;
import com.bookstore.common.dto.OrderItemDto;
import com.bookstore.order.document.Order;
import com.bookstore.order.document.OrderItem;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    private final ModelMapper modelMapper;

    public OrderMapper() {
        this.modelMapper = new ModelMapper();
    }

    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDto dto = modelMapper.map(order, OrderDto.class);
        
        if (order.getItems() != null) {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }
        
        return dto;
    }

    public Order toDocument(OrderDto dto) {
        if (dto == null) {
            return null;
        }
        
        Order order = modelMapper.map(dto, Order.class);
        
        if (dto.getItems() != null) {
            List<OrderItem> items = dto.getItems().stream()
                    .map(this::toItemDocument)
                    .collect(Collectors.toList());
            order.setItems(items);
        }
        
        return order;
    }

    public OrderItemDto toItemDto(OrderItem item) {
        return modelMapper.map(item, OrderItemDto.class);
    }

    public OrderItem toItemDocument(OrderItemDto dto) {
        return modelMapper.map(dto, OrderItem.class);
    }

    public List<OrderDto> toDtoList(List<Order> orders) {
        return orders.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
