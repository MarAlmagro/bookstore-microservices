package com.bookstore.common.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response Dto for consistent error formatting across all microservices.
 *
 * This class is returned by the GlobalExceptionHandler when any exception occurs,
 * providing clients with structured error information including timestamp, HTTP status,
 * error type, message, and the request path that caused the error.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp when the error occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * HTTP status code (e.g., 404, 400, 401, 500)
     */
    private int status;

    /**
     * Short error type description (e.g., "Not Found", "Bad Request")
     */
    private String error;

    /**
     * Detailed error message explaining what went wrong
     */
    private String message;

    /**
     * The request path that caused the error
     */
    private String path;
}
