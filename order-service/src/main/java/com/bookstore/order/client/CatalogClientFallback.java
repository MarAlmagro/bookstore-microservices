package com.bookstore.order.client;

import com.bookstore.common.dto.BookDto;
import com.bookstore.common.exception.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class CatalogClientFallback implements CatalogClient {

    @Override
    public BookDto getBookById(Long id) {
        log.error("Catalog service is unavailable. Circuit breaker activated for book id: {}", id);
        throw new ServiceUnavailableException("Catalog service is temporarily unavailable. Please try again later.");
    }

    @Override
    public List<BookDto> getBooksByIds(List<Long> ids) {
        log.error("Catalog service is unavailable. Circuit breaker activated for batch book fetch: {}", ids);
        throw new ServiceUnavailableException("Catalog service is temporarily unavailable. Please try again later.");
    }
}
