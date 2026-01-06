package com.bookstore.catalog.mapper;

import com.bookstore.catalog.entity.Book;
import com.bookstore.common.dto.BookDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Book entity and BookDto.
 * <p>
 * This class provides manual mapping methods to transform data between
 * the persistence layer (Entity) and the presentation layer (Dto).
 * </p>
 * <p>
 * Manual mapping is used instead of MapStruct to maintain simplicity
 * and avoid additional build-time code generation dependencies.
 * </p>
 */
@Component
public class BookMapper {

    /**
     * Convert a Book entity to BookDto
     *
     * @param book the book entity
     * @return the book Dto, or null if input is null
     */
    public BookDto toDto(Book book) {
        if (book == null) {
            return null;
        }

        return BookDto.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .description(book.getDescription())
                .price(book.getPrice())
                .stock(book.getStock())
                .category(book.getCategory())
                .build();
    }

    /**
     * Convert a BookDto to Book entity
     *
     * @param bookDto the book Dto
     * @return the book entity, or null if input is null
     */
    public Book toEntity(BookDto bookDto) {
        if (bookDto == null) {
            return null;
        }

        return Book.builder()
                .id(bookDto.getId())
                .isbn(bookDto.getIsbn())
                .title(bookDto.getTitle())
                .author(bookDto.getAuthor())
                .description(bookDto.getDescription())
                .price(bookDto.getPrice())
                .stock(bookDto.getStock())
                .category(bookDto.getCategory())
                .build();
    }

    /**
     * Convert a list of Book entities to a list of BookDtos
     *
     * @param books the list of book entities
     * @return the list of book Dtos
     */
    public List<BookDto> toDtoList(List<Book> books) {
        if (books == null) {
            return null;
        }

        return books.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of BookDtos to a list of Book entities
     *
     * @param bookDtos the list of book Dtos
     * @return the list of book entities
     */
    public List<Book> toEntityList(List<BookDto> bookDtos) {
        if (bookDtos == null) {
            return null;
        }

        return bookDtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing Book entity with data from BookDto
     * <p>
     * This method updates only the modifiable fields and preserves
     * the ID and audit fields (createdAt, updatedAt).
     * </p>
     *
     * @param bookDto the source Dto with updated data
     * @param book the target entity to update
     */
    public void updateEntityFromDto(BookDto bookDto, Book book) {
        if (bookDto == null || book == null) {
            return;
        }

        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setDescription(bookDto.getDescription());
        book.setPrice(bookDto.getPrice());
        book.setStock(bookDto.getStock());
        book.setCategory(bookDto.getCategory());
    }
}
