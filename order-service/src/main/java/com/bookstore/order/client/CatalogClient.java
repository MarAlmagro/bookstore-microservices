package com.bookstore.order.client;

import com.bookstore.common.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "catalog-service", url = "${catalog.service.url:}", fallback = CatalogClientFallback.class)
public interface CatalogClient {

    @GetMapping("/api/v1/books/{id}")
    BookDto getBookById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/books/batch")
    List<BookDto> getBooksByIds(@RequestParam("ids") List<Long> ids);
}
