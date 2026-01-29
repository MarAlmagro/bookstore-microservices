package com.bookstore.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private static final String TEST_PATH = "/api/test";

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=" + TEST_PATH);
    }

    @Test
    @DisplayName("handleResourceNotFoundException should return 404 response")
    void handleResourceNotFoundException_shouldReturn404() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Book not found with id: 123");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("Book not found with id: 123");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleInvalidRequestException should return 400 response")
    void handleInvalidRequestException_shouldReturn400() {
        InvalidRequestException exception = new InvalidRequestException("Invalid ISBN format");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInvalidRequestException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid ISBN format");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleUnauthorizedException should return 401 response")
    void handleUnauthorizedException_shouldReturn401() {
        UnauthorizedException exception = new UnauthorizedException("Invalid JWT token");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnauthorizedException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getError()).isEqualTo("Unauthorized");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid JWT token");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleValidationException should return 400 with field errors")
    void handleValidationException_shouldReturn400WithFieldErrors() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "bookDto");
        bindingResult.addError(new FieldError("bookDto", "title", "Title is required"));
        bindingResult.addError(new FieldError("bookDto", "price", "Price must be greater than 0"));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("Validation failed");
        assertThat(response.getBody().getMessage()).contains("title: Title is required");
        assertThat(response.getBody().getMessage()).contains("price: Price must be greater than 0");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleValidationException should handle single field error")
    void handleValidationException_withSingleError_shouldFormatCorrectly() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "userDto");
        bindingResult.addError(new FieldError("userDto", "email", "Email must be valid"));

        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed: email: Email must be valid");
    }

    @Test
    @DisplayName("handleGlobalException should return 500 response")
    void handleGlobalException_shouldReturn500() {
        Exception exception = new RuntimeException("Unexpected database error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).contains("An unexpected error occurred");
        assertThat(response.getBody().getMessage()).contains("Unexpected database error");
        assertThat(response.getBody().getPath()).isEqualTo(TEST_PATH);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("handleGlobalException should handle NullPointerException")
    void handleGlobalException_withNullPointer_shouldReturn500() {
        NullPointerException exception = new NullPointerException("Object reference is null");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, webRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
    }

    @Test
    @DisplayName("error response should have correct structure")
    void errorResponse_shouldHaveCorrectStructure() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        ErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getStatus()).isPositive();
        assertThat(errorResponse.getError()).isNotBlank();
        assertThat(errorResponse.getMessage()).isNotBlank();
        assertThat(errorResponse.getPath()).isNotBlank();
    }

    @Test
    @DisplayName("path should be extracted correctly from WebRequest")
    void pathExtraction_shouldRemoveUriPrefix() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/books/123");
        ResourceNotFoundException exception = new ResourceNotFoundException("Not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFoundException(exception, webRequest);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPath()).isEqualTo("/api/books/123");
    }
}
