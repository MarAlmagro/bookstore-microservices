package com.bookstore.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 *
 * This exception should be used when attempting to retrieve a resource by ID or
 * other unique identifier, but the resource does not exist in the database.
 *
 * Examples:
 * - Book not found by ID
 * - Order not found by ID
 * - User not found by email
 *
 * The GlobalExceptionHandler will catch this exception and return an HTTP 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message explaining which resource was not found
     * @param cause   the cause of the exception
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
