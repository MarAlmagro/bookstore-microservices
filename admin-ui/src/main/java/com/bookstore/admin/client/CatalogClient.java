package com.bookstore.admin.client;

import com.bookstore.admin.dto.BookDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "catalog-client", url = "http://localhost:8080", path = "/api/books")
public interface CatalogClient {

    @GetMapping
    List<BookDTO> getAllBooks();

    @GetMapping("/{id}")
    BookDTO getBookById(@PathVariable("id") Long id);

    @PostMapping
    BookDTO createBook(@RequestBody BookDTO book);

    @PutMapping("/{id}")
    BookDTO updateBook(@PathVariable("id") Long id, @RequestBody BookDTO book);

    @DeleteMapping("/{id}")
    void deleteBook(@PathVariable("id") Long id);

    @GetMapping("/search")
    List<BookDTO> searchBooks(@RequestParam("query") String query);
}
