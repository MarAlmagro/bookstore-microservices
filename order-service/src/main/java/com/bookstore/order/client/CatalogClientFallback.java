package com.bookstore.order.client;

import com.bookstore.common.dto.BookDTO;
import com.bookstore.common.exception.InvalidRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CatalogClientFallback implements CatalogClient {

    @Override
    public BookDTO getBookById(Long id) {
        log.error("Catalog service is unavailable. Circuit breaker activated for book id: {}", id);
        throw new InvalidRequestException("Catalog service is temporarily unavailable. Please try again later.");
    }
}
