package com.bookstore.common.util;

import com.bookstore.common.dto.PageRequestDto;
import com.bookstore.common.dto.PageResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PageMapper Unit Tests")
class PageMapperTest {

    @Test
    @DisplayName("toPageResponse should map Page to PageResponseDto with valid data")
    void toPageResponse_withValidPage_shouldMapCorrectly() {
        List<String> content = List.of("item1", "item2", "item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 23);

        PageResponseDto<String> result = PageMapper.toPageResponse(page, String.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(23);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    @DisplayName("toPageResponse should handle empty Page correctly")
    void toPageResponse_withEmptyPage_shouldMapCorrectly() {
        Page<String> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        PageResponseDto<String> result = PageMapper.toPageResponse(emptyPage, String.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("toPageResponse should handle last page correctly")
    void toPageResponse_withLastPage_shouldSetLastTrue() {
        List<String> content = List.of("item1", "item2");
        Page<String> lastPage = new PageImpl<>(content, PageRequest.of(2, 10), 22);

        PageResponseDto<String> result = PageMapper.toPageResponse(lastPage, String.class);

        assertThat(result).isNotNull();
        assertThat(result.getPage()).isEqualTo(2);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("toPageResponse should handle single page correctly")
    void toPageResponse_withSinglePage_shouldSetFirstAndLastTrue() {
        List<String> content = List.of("item1", "item2");
        Page<String> singlePage = new PageImpl<>(content, PageRequest.of(0, 10), 2);

        PageResponseDto<String> result = PageMapper.toPageResponse(singlePage, String.class);

        assertThat(result).isNotNull();
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    @DisplayName("toPageable should create Pageable with ascending sort")
    void toPageable_withAscendingSort_shouldCreateCorrectPageable() {
        Pageable result = PageMapper.toPageable(0, 20, "title", "asc");

        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getSort().getOrderFor("title")).isNotNull();
        assertThat(result.getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("toPageable should create Pageable with descending sort")
    void toPageable_withDescendingSort_shouldCreateCorrectPageable() {
        Pageable result = PageMapper.toPageable(1, 10, "createdAt", "desc");

        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(result.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("toPageable should default to descending when sortDir is not 'asc'")
    void toPageable_withInvalidSortDir_shouldDefaultToDescending() {
        Pageable result = PageMapper.toPageable(0, 20, "name", "invalid");

        assertThat(result).isNotNull();
        assertThat(result.getSort().getOrderFor("name")).isNotNull();
        assertThat(result.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("toPageable should handle case-insensitive 'ASC'")
    void toPageable_withUppercaseAsc_shouldCreateAscendingSort() {
        Pageable result = PageMapper.toPageable(0, 20, "price", "ASC");

        assertThat(result).isNotNull();
        assertThat(result.getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("toPageable with PageRequestDto should map correctly")
    void toPageable_withPageRequestDto_shouldMapCorrectly() {
        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(2)
                .size(15)
                .sortBy("author")
                .sortDir("asc")
                .build();

        Pageable result = PageMapper.toPageable(pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(2);
        assertThat(result.getPageSize()).isEqualTo(15);
        assertThat(result.getSort().getOrderFor("author")).isNotNull();
        assertThat(result.getSort().getOrderFor("author").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("toPageable with PageRequestDto should use default values")
    void toPageable_withDefaultPageRequestDto_shouldUseDefaults() {
        PageRequestDto pageRequest = PageRequestDto.builder().build();

        Pageable result = PageMapper.toPageable(pageRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(20);
        assertThat(result.getSort().getOrderFor("createdAt")).isNotNull();
        assertThat(result.getSort().getOrderFor("createdAt").getDirection()).isEqualTo(Sort.Direction.DESC);
    }
}
