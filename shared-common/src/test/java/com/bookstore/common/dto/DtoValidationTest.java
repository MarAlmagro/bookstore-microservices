package com.bookstore.common.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO Validation Tests")
class DtoValidationTest {

    private Validator validator;

    private static final String VALID_ISBN = "1234567890";
    private static final String VALID_TITLE = "Test Book";
    private static final String VALID_AUTHOR = "Test Author";
    private static final String VALID_PRICE = "19.99";
    private static final String VALID_CATEGORY = "Fiction";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_ROLE = "CUSTOMER";

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("BookDto should pass validation with valid data")
    void bookDto_withValidData_shouldPassValidation() {
        BookDto bookDto = BookDto.builder()
                .isbn(VALID_ISBN)
                .title(VALID_TITLE)
                .author(VALID_AUTHOR)
                .description("Test Description")
                .price(new BigDecimal(VALID_PRICE))
                .stock(10)
                .category(VALID_CATEGORY)
                .build();

        Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("BookDto should fail validation when ISBN is blank")
    void bookDto_withBlankIsbn_shouldFailValidation() {
        BookDto bookDto = BookDto.builder()
                .isbn("")
                .title(VALID_TITLE)
                .author(VALID_AUTHOR)
                .price(new BigDecimal(VALID_PRICE))
                .stock(10)
                .category(VALID_CATEGORY)
                .build();

        Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("isbn"));
    }

    @Test
    @DisplayName("BookDto should fail validation when ISBN is too short")
    void bookDto_withShortIsbn_shouldFailValidation() {
        BookDto bookDto = BookDto.builder()
                .isbn("123")
                .title(VALID_TITLE)
                .author(VALID_AUTHOR)
                .price(new BigDecimal(VALID_PRICE))
                .stock(10)
                .category(VALID_CATEGORY)
                .build();

        Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("isbn"));
    }

    @Test
    @DisplayName("BookDto should fail validation when title is blank")
    void bookDto_withBlankTitle_shouldFailValidation() {
        BookDto bookDto = BookDto.builder()
                .isbn(VALID_ISBN)
                .title("")
                .author(VALID_AUTHOR)
                .price(new BigDecimal(VALID_PRICE))
                .stock(10)
                .category(VALID_CATEGORY)
                .build();

        Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("title"));
    }

    @Test
    @DisplayName("BookDto should fail validation when price is zero")
    void bookDto_withZeroPrice_shouldFailValidation() {
        BookDto bookDto = BookDto.builder()
                .isbn(VALID_ISBN)
                .title(VALID_TITLE)
                .author(VALID_AUTHOR)
                .price(BigDecimal.ZERO)
                .stock(10)
                .category(VALID_CATEGORY)
                .build();

        Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("price"));
    }

    @Test
    @DisplayName("BookDto should fail validation when stock is negative")
    void bookDto_withNegativeStock_shouldFailValidation() {
        BookDto bookDto = BookDto.builder()
                .isbn(VALID_ISBN)
                .title(VALID_TITLE)
                .author(VALID_AUTHOR)
                .price(new BigDecimal(VALID_PRICE))
                .stock(-1)
                .category(VALID_CATEGORY)
                .build();

        Set<ConstraintViolation<BookDto>> violations = validator.validate(bookDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("stock"));
    }

    @Test
    @DisplayName("UserDto should pass validation with valid data")
    void userDto_withValidData_shouldPassValidation() {
        UserDto userDto = UserDto.builder()
                .email(VALID_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .role(VALID_ROLE)
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("UserDto should fail validation when email is invalid")
    void userDto_withInvalidEmail_shouldFailValidation() {
        UserDto userDto = UserDto.builder()
                .email("invalid-email")
                .firstName("John")
                .lastName("Doe")
                .role(VALID_ROLE)
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("UserDto should fail validation when firstName is blank")
    void userDto_withBlankFirstName_shouldFailValidation() {
        UserDto userDto = UserDto.builder()
                .email(VALID_EMAIL)
                .firstName("")
                .lastName("Doe")
                .role(VALID_ROLE)
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstName"));
    }

    @Test
    @DisplayName("UserDto should fail validation when role is blank")
    void userDto_withBlankRole_shouldFailValidation() {
        UserDto userDto = UserDto.builder()
                .email(VALID_EMAIL)
                .firstName("John")
                .lastName("Doe")
                .role("")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("role"));
    }

    @Test
    @DisplayName("OrderDto should pass validation with valid data")
    void orderDto_withValidData_shouldPassValidation() {
        OrderItemDto item = OrderItemDto.builder()
                .bookId(1L)
                .quantity(2)
                .build();

        OrderDto orderDto = OrderDto.builder()
                .userId(1L)
                .item(item)
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(orderDto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("OrderDto should fail validation when userId is null")
    void orderDto_withNullUserId_shouldFailValidation() {
        OrderItemDto item = OrderItemDto.builder()
                .bookId(1L)
                .quantity(2)
                .build();

        OrderDto orderDto = OrderDto.builder()
                .userId(null)
                .item(item)
                .build();

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(orderDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("userId"));
    }

    @Test
    @DisplayName("OrderDto should fail validation when items list is empty")
    void orderDto_withEmptyItems_shouldFailValidation() {
        OrderDto orderDto = OrderDto.builder()
                .userId(1L)
                .build();
        orderDto.setItems(List.of());

        Set<ConstraintViolation<OrderDto>> violations = validator.validate(orderDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("items"));
    }

    @Test
    @DisplayName("OrderItemDto should pass validation with valid data")
    void orderItemDto_withValidData_shouldPassValidation() {
        OrderItemDto orderItemDto = OrderItemDto.builder()
                .bookId(1L)
                .quantity(5)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(orderItemDto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("OrderItemDto should fail validation when bookId is null")
    void orderItemDto_withNullBookId_shouldFailValidation() {
        OrderItemDto orderItemDto = OrderItemDto.builder()
                .bookId(null)
                .quantity(5)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(orderItemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("bookId"));
    }

    @Test
    @DisplayName("OrderItemDto should fail validation when quantity is zero")
    void orderItemDto_withZeroQuantity_shouldFailValidation() {
        OrderItemDto orderItemDto = OrderItemDto.builder()
                .bookId(1L)
                .quantity(0)
                .build();

        Set<ConstraintViolation<OrderItemDto>> violations = validator.validate(orderItemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("quantity"));
    }

    @Test
    @DisplayName("AuthRequestDto should pass validation with valid data")
    void authRequestDto_withValidData_shouldPassValidation() {
        AuthRequestDto authRequestDto = AuthRequestDto.builder()
                .email(VALID_EMAIL)
                .password("password123")
                .build();

        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(authRequestDto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("AuthRequestDto should fail validation when email is invalid")
    void authRequestDto_withInvalidEmail_shouldFailValidation() {
        AuthRequestDto authRequestDto = AuthRequestDto.builder()
                .email("not-an-email")
                .password("password123")
                .build();

        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(authRequestDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("AuthRequestDto should fail validation when password is too short")
    void authRequestDto_withShortPassword_shouldFailValidation() {
        AuthRequestDto authRequestDto = AuthRequestDto.builder()
                .email(VALID_EMAIL)
                .password("12345")
                .build();

        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(authRequestDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("AuthRequestDto should fail validation when password is blank")
    void authRequestDto_withBlankPassword_shouldFailValidation() {
        AuthRequestDto authRequestDto = AuthRequestDto.builder()
                .email(VALID_EMAIL)
                .password("")
                .build();

        Set<ConstraintViolation<AuthRequestDto>> violations = validator.validate(authRequestDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
