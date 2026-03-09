package com.bookstore.common.exception;

/**
 * Exception thrown when a user attempts to access a resource without proper
 * authentication or authorization.
 *
 * This exception should be used when:
 * - A user is not authenticated (no valid JWT token)
 * - A user is authenticated but lacks the required role/permissions
 * - A JWT token is expired or invalid
 * - A user tries to access another user's private resources
 *
 * Examples:
 * - Accessing protected endpoints without a JWT token
 * - Customer trying to access admin-only endpoints
 * - User trying to view another user's orders
 * - Expired or malformed JWT token
 *
 * The GlobalExceptionHandler will catch this exception and return an HTTP 401
 * Unauthorized response.
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructs a new UnauthorizedException with the specified detail message.
     *
     * @param message the detail message explaining the authorization failure
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructs a new UnauthorizedException with the specified detail message and
     * cause.
     *
     * @param message the detail message explaining the authorization failure
     * @param cause   the cause of the exception
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
