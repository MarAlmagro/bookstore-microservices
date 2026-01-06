package com.bookstore.admin.client;

import com.bookstore.admin.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "catalog-client", url = "http://localhost:8080", path = "/api/books")
public interface CatalogClient {

    @GetMapping
    List<BookDto> getAllBooks();

    @GetMapping("/{id}")
    BookDto getBookById(@PathVariable("id") Long id);

    @PostMapping
    BookDto createBook(@RequestBody BookDto book);

    @PutMapping("/{id}")
    BookDto updateBook(@PathVariable("id") Long id, @RequestBody BookDto book);

    @DeleteMapping("/{id}")
    void deleteBook(@PathVariable("id") Long id);

    @GetMapping("/search")
    List<BookDto> searchBooks(@RequestParam("query") String query);
}
