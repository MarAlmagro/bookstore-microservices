package com.bookstore.order.client;

import com.bookstore.common.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service", fallback = CatalogClientFallback.class)
public interface CatalogClient {

    @GetMapping("/api/v1/books/{id}")
    BookDto getBookById(@PathVariable("id") Long id);
}
