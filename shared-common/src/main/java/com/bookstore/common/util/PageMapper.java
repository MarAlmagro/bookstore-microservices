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

    public static <T> PageResponseDto<T> toPageResponse(Page<?> page, Class<T> type) {
        PageResponseDto<T> response = new PageResponseDto<>();
        response.setContent(null); // Will be set by caller
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setSize(page.getSize());
        response.setPage(page.getNumber());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        return response;
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
