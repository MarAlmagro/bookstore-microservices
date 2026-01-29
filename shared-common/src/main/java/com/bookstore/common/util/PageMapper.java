package com.bookstore.common.util;

import com.bookstore.common.dto.PageResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PageMapper {

    private PageMapper() {
    }

    public static <T> PageResponseDto<T> toPageResponse(Page<T> page) {
        return PageResponseDto.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public static Pageable toPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }

    public static Pageable toPageable(com.bookstore.common.dto.PageRequestDto pageRequest) {
        return toPageable(
                pageRequest.getPage(),
                pageRequest.getSize(),
                pageRequest.getSortBy(),
                pageRequest.getSortDir()
        );
    }
}
