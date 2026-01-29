package com.bookstore.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDto {
    
    @Min(0)
    private int page = 0;
    
    @Min(1)
    @Max(100)
    private int size = 20;
    
    private String sortBy = "createdAt";
    
    private String sortDir = "desc";
}
