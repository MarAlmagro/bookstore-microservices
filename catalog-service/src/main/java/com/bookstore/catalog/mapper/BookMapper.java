package com.bookstore.catalog.mapper;

import com.bookstore.catalog.entity.Book;
import com.bookstore.common.dto.BookDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Book entity and BookDTO.
 * <p>
 * This class provides manual mapping methods to transform data between
 * the persistence layer (Entity) and the presentation layer (DTO).
 * </p>
 * <p>
 * Manual mapping is used instead of MapStruct to maintain simplicity
 * and avoid additional build-time code generation dependencies.
 * </p>
 */
@Component
public class BookMapper {

    /**
     * Convert a Book entity to BookDTO
     *
     * @param book the book entity
     * @return the book DTO, or null if input is null
     */
    public BookDTO toDTO(Book book) {
        if (book == null) {
            return null;
        }

        return BookDTO.builder()
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
     * Convert a BookDTO to Book entity
     *
     * @param bookDTO the book DTO
     * @return the book entity, or null if input is null
     */
    public Book toEntity(BookDTO bookDTO) {
        if (bookDTO == null) {
            return null;
        }

        return Book.builder()
                .id(bookDTO.getId())
                .isbn(bookDTO.getIsbn())
                .title(bookDTO.getTitle())
                .author(bookDTO.getAuthor())
                .description(bookDTO.getDescription())
                .price(bookDTO.getPrice())
                .stock(bookDTO.getStock())
                .category(bookDTO.getCategory())
                .build();
    }

    /**
     * Convert a list of Book entities to a list of BookDTOs
     *
     * @param books the list of book entities
     * @return the list of book DTOs
     */
    public List<BookDTO> toDTOList(List<Book> books) {
        if (books == null) {
            return null;
        }

        return books.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of BookDTOs to a list of Book entities
     *
     * @param bookDTOs the list of book DTOs
     * @return the list of book entities
     */
    public List<Book> toEntityList(List<BookDTO> bookDTOs) {
        if (bookDTOs == null) {
            return null;
        }

        return bookDTOs.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing Book entity with data from BookDTO
     * <p>
     * This method updates only the modifiable fields and preserves
     * the ID and audit fields (createdAt, updatedAt).
     * </p>
     *
     * @param bookDTO the source DTO with updated data
     * @param book the target entity to update
     */
    public void updateEntityFromDTO(BookDTO bookDTO, Book book) {
        if (bookDTO == null || book == null) {
            return;
        }

        book.setIsbn(bookDTO.getIsbn());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setDescription(bookDTO.getDescription());
        book.setPrice(bookDTO.getPrice());
        book.setStock(bookDTO.getStock());
        book.setCategory(bookDTO.getCategory());
    }
}
