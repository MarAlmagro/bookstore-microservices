package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookImportDTO {
    private String isbn;
    private String title;
    private String author;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
}
