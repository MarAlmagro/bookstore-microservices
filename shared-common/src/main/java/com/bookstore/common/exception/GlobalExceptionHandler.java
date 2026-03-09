package com.bookstore.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Global exception handler for all microservices.
 *
 * This class uses Spring's @ControllerAdvice to centrally handle exceptions
 * thrown by any controller in the application. It converts exceptions into
 * consistent ErrorResponse objects with appropriate HTTP status codes.
 *
 * All services should import this shared-common module to benefit from
 * consistent error handling across the entire microservices architecture.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Handles ResourceNotFoundException - returns HTTP 404 Not Found
         *
         * @param ex      the exception
         * @param request the web request
         * @return ResponseEntity with ErrorResponse and 404 status
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
                        ResourceNotFoundException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.NOT_FOUND.value())
                                .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        /**
         * Handles InvalidRequestException - returns HTTP 400 Bad Request
         *
         * @param ex      the exception
         * @param request the web request
         * @return ResponseEntity with ErrorResponse and 400 status
         */
        @ExceptionHandler(InvalidRequestException.class)
        public ResponseEntity<ErrorResponse> handleInvalidRequestException(
                        InvalidRequestException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles UnauthorizedException - returns HTTP 401 Unauthorized
         *
         * @param ex      the exception
         * @param request the web request
         * @return ResponseEntity with ErrorResponse and 401 status
         */
        @ExceptionHandler(UnauthorizedException.class)
        public ResponseEntity<ErrorResponse> handleUnauthorizedException(
                        UnauthorizedException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        /**
         * Handles ServiceUnavailableException - returns HTTP 503 Service Unavailable
         *
         * @param ex      the exception
         * @param request the web request
         * @return ResponseEntity with ErrorResponse and 503 status
         */
        @ExceptionHandler(ServiceUnavailableException.class)
        public ResponseEntity<ErrorResponse> handleServiceUnavailableException(
                        ServiceUnavailableException ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                                .error(HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase())
                                .message(ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
        }

        /**
         * Handles validation errors from @Valid annotation - returns HTTP 400 Bad
         * Request
         *
         * @param ex      the MethodArgumentNotValidException containing validation
         *                errors
         * @param request the web request
         * @return ResponseEntity with ErrorResponse and 400 status
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        MethodArgumentNotValidException ex,
                        WebRequest request) {

                String validationErrors = ex.getBindingResult().getAllErrors().stream()
                                .map(error -> {
                                        String fieldName = ((FieldError) error).getField();
                                        String errorMessage = error.getDefaultMessage();
                                        return fieldName + ": " + errorMessage;
                                })
                                .collect(Collectors.joining(", "));

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .message("Validation failed: " + validationErrors)
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        /**
         * Handles all other uncaught exceptions - returns HTTP 500 Internal Server
         * Error
         *
         * @param ex      the exception
         * @param request the web request
         * @return ResponseEntity with ErrorResponse and 500 status
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGlobalException(
                        Exception ex,
                        WebRequest request) {

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                                .message("An unexpected error occurred: " + ex.getMessage())
                                .path(request.getDescription(false).replace("uri=", ""))
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}
