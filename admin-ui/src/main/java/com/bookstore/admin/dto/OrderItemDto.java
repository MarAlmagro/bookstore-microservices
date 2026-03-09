package com.bookstore.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    private Long bookId;
    private String bookTitle;
    private Integer quantity;
    private BigDecimal price;
}
