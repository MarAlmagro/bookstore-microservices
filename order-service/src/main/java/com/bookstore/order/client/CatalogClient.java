package com.bookstore.order.client;

import com.bookstore.common.dto.BookDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/api/v1/books/{id}")
    BookDTO getBookById(@PathVariable("id") Long id);
}
