package com.bookstore.common.exception;

/**
 * Exception thrown when a client request is invalid or contains bad data.
 *
 * This exception should be used when the request payload fails business validation rules
 * or contains semantically incorrect data (beyond basic @Valid annotation checks).
 *
 * Examples:
 * - Attempting to order more books than available in stock
 * - Invalid ISBN format
 * - Negative price or quantity values
 * - Business rule violations (e.g., cannot cancel a shipped order)
 *
 * The GlobalExceptionHandler will catch this exception and return an HTTP 400 Bad Request response.
 */
public class InvalidRequestException extends RuntimeException {

    /**
     * Constructs a new InvalidRequestException with the specified detail message.
     *
     * @param message the detail message explaining why the request is invalid
     */
    public InvalidRequestException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidRequestException with the specified detail message and cause.
     *
     * @param message the detail message explaining why the request is invalid
     * @param cause   the cause of the exception
     */
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
