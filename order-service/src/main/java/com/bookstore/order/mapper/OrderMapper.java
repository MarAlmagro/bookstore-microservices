package com.bookstore.order.mapper;

import com.bookstore.common.dto.OrderDTO;
import com.bookstore.common.dto.OrderItemDTO;
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

    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDTO dto = modelMapper.map(order, OrderDTO.class);
        
        if (order.getItems() != null) {
            List<OrderItemDTO> itemDTOs = order.getItems().stream()
                    .map(this::toItemDTO)
                    .collect(Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        return dto;
    }

    public Order toDocument(OrderDTO dto) {
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

    public OrderItemDTO toItemDTO(OrderItem item) {
        return modelMapper.map(item, OrderItemDTO.class);
    }

    public OrderItem toItemDocument(OrderItemDTO dto) {
        return modelMapper.map(dto, OrderItem.class);
    }

    public List<OrderDTO> toDTOList(List<Order> orders) {
        return orders.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
