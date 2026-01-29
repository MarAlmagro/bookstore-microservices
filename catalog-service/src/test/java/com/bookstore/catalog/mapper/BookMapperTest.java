package com.bookstore.catalog.mapper;

import com.bookstore.catalog.entity.Book;
import com.bookstore.catalog.fixtures.BookTestFixtures;
import com.bookstore.common.dto.BookDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookMapper Unit Tests")
class BookMapperTest {

    private BookMapper bookMapper;

    @BeforeEach
    void setUp() {
        bookMapper = new BookMapper();
    }

    @Test
    @DisplayName("toDto should map Book entity to BookDto correctly")
    void toDto_withValidBook_shouldMapAllFields() {
        Book book = BookTestFixtures.createSampleBook();

        BookDto result = bookMapper.toDto(book);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(book.getId());
        assertThat(result.getIsbn()).isEqualTo(book.getIsbn());
        assertThat(result.getTitle()).isEqualTo(book.getTitle());
        assertThat(result.getAuthor()).isEqualTo(book.getAuthor());
        assertThat(result.getDescription()).isEqualTo(book.getDescription());
        assertThat(result.getPrice()).isEqualTo(book.getPrice());
        assertThat(result.getStock()).isEqualTo(book.getStock());
        assertThat(result.getCategory()).isEqualTo(book.getCategory());
    }

    @Test
    @DisplayName("toDto should return null when Book is null")
    void toDto_withNullBook_shouldReturnNull() {
        BookDto result = bookMapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toEntity should map BookDto to Book entity correctly")
    void toEntity_withValidBookDto_shouldMapAllFields() {
        BookDto bookDto = BookTestFixtures.createSampleBookDto();

        Book result = bookMapper.toEntity(bookDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(bookDto.getId());
        assertThat(result.getIsbn()).isEqualTo(bookDto.getIsbn());
        assertThat(result.getTitle()).isEqualTo(bookDto.getTitle());
        assertThat(result.getAuthor()).isEqualTo(bookDto.getAuthor());
        assertThat(result.getDescription()).isEqualTo(bookDto.getDescription());
        assertThat(result.getPrice()).isEqualTo(bookDto.getPrice());
        assertThat(result.getStock()).isEqualTo(bookDto.getStock());
        assertThat(result.getCategory()).isEqualTo(bookDto.getCategory());
    }

    @Test
    @DisplayName("toEntity should return null when BookDto is null")
    void toEntity_withNullBookDto_shouldReturnNull() {
        Book result = bookMapper.toEntity(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toDtoList should convert list of Books to list of BookDtos")
    void toDtoList_withValidBookList_shouldMapAllBooks() {
        List<Book> books = Arrays.asList(
                BookTestFixtures.createSampleBook(),
                BookTestFixtures.createSecondSampleBook()
        );

        List<BookDto> result = bookMapper.toDtoList(books);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Effective Java");
        assertThat(result.get(1).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    @DisplayName("toDtoList should return empty list when input is empty")
    void toDtoList_withEmptyList_shouldReturnEmptyList() {
        List<BookDto> result = bookMapper.toDtoList(Collections.emptyList());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toDtoList should return null when input is null")
    void toDtoList_withNullList_shouldReturnNull() {
        List<BookDto> result = bookMapper.toDtoList(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("toEntityList should convert list of BookDtos to list of Books")
    void toEntityList_withValidBookDtoList_shouldMapAllBookDtos() {
        List<BookDto> bookDtos = Arrays.asList(
                BookTestFixtures.createSampleBookDto(),
                BookTestFixtures.createNewBookDto()
        );

        List<Book> result = bookMapper.toEntityList(bookDtos);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Effective Java");
        assertThat(result.get(1).getTitle()).isEqualTo("New Test Book");
    }

    @Test
    @DisplayName("toEntityList should return empty list when input is empty")
    void toEntityList_withEmptyList_shouldReturnEmptyList() {
        List<Book> result = bookMapper.toEntityList(Collections.emptyList());

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("toEntityList should return null when input is null")
    void toEntityList_withNullList_shouldReturnNull() {
        List<Book> result = bookMapper.toEntityList(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("updateEntityFromDto should update Book entity with BookDto data")
    void updateEntityFromDto_withValidInputs_shouldUpdateAllFields() {
        Book book = BookTestFixtures.createSampleBook();
        BookDto updateDto = BookTestFixtures.createUpdateBookDto();

        bookMapper.updateEntityFromDto(updateDto, book);

        assertThat(book.getId()).isEqualTo(1L);
        assertThat(book.getIsbn()).isEqualTo(updateDto.getIsbn());
        assertThat(book.getTitle()).isEqualTo("Effective Java - Updated Edition");
        assertThat(book.getAuthor()).isEqualTo(updateDto.getAuthor());
        assertThat(book.getDescription()).isEqualTo("Updated description with new content");
        assertThat(book.getPrice()).isEqualTo(new BigDecimal("49.99"));
        assertThat(book.getStock()).isEqualTo(120);
        assertThat(book.getCategory()).isEqualTo(updateDto.getCategory());
    }

    @Test
    @DisplayName("updateEntityFromDto should not update when BookDto is null")
    void updateEntityFromDto_withNullBookDto_shouldNotUpdate() {
        Book book = BookTestFixtures.createSampleBook();
        String originalTitle = book.getTitle();

        bookMapper.updateEntityFromDto(null, book);

        assertThat(book.getTitle()).isEqualTo(originalTitle);
    }

    @Test
    @DisplayName("updateEntityFromDto should not update when Book is null")
    void updateEntityFromDto_withNullBook_shouldNotThrowException() {
        BookDto updateDto = BookTestFixtures.createUpdateBookDto();

        bookMapper.updateEntityFromDto(updateDto, null);
    }

    @Test
    @DisplayName("updateEntityFromDto should handle empty strings")
    void updateEntityFromDto_withEmptyStrings_shouldUpdateWithEmptyValues() {
        Book book = BookTestFixtures.createSampleBook();
        BookDto updateDto = BookDto.builder()
                .isbn("")
                .title("")
                .author("")
                .description("")
                .price(BigDecimal.ZERO)
                .stock(0)
                .category("")
                .build();

        bookMapper.updateEntityFromDto(updateDto, book);

        assertThat(book.getIsbn()).isEmpty();
        assertThat(book.getTitle()).isEmpty();
        assertThat(book.getAuthor()).isEmpty();
        assertThat(book.getDescription()).isEmpty();
        assertThat(book.getPrice()).isEqualTo(BigDecimal.ZERO);
        assertThat(book.getStock()).isZero();
        assertThat(book.getCategory()).isEmpty();
    }
}
