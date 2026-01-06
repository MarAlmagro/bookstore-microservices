package com.bookstore.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long id;
    private String isbn;
    private String title;
    private String author;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String category;
}
