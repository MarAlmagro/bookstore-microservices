package com.bookstore.catalog.batch;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.repository.BookRepository;
import com.bookstore.common.dto.BookImportDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "true", matchIfMissing = true)
public class BookImportProcessor implements ItemProcessor<BookImportDto, Book> {

    private final BookRepository bookRepository;

    @Override
    public Book process(BookImportDto dto) throws Exception {
        log.debug("Processing book import for ISBN: {}", dto.getIsbn());
        
        Optional<Book> existingBook = bookRepository.findByIsbn(dto.getIsbn());
        
        if (existingBook.isPresent()) {
            Book book = existingBook.get();
            book.setPrice(dto.getPrice());
            book.setStock(dto.getStock());
            book.setTitle(dto.getTitle());
            book.setAuthor(dto.getAuthor());
            book.setDescription(dto.getDescription());
            book.setCategory(dto.getCategory());
            log.debug("Updating existing book with ISBN: {}", dto.getIsbn());
            return book;
        } else {
            Book newBook = Book.builder()
                    .isbn(dto.getIsbn())
                    .title(dto.getTitle())
                    .author(dto.getAuthor())
                    .description(dto.getDescription())
                    .price(dto.getPrice())
                    .stock(dto.getStock())
                    .category(dto.getCategory())
                    .build();
            log.debug("Creating new book with ISBN: {}", dto.getIsbn());
            return newBook;
        }
    }
}
