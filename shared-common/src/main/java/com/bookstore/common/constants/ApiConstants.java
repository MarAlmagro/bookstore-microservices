package com.bookstore.common.constants;

/**
 * Central repository for API-related constants used across all microservices.
 * <p>
 * This ensures consistent API versioning and endpoint naming conventions.
 * </p>
 */
public final class ApiConstants {

    /**
     * Base path prefix for all API version 1 endpoints
     * <p>
     * Usage example: @RequestMapping(ApiConstants.API_V1_PREFIX + "/books")
     * Results in: /api/v1/books
     * </p>
     */
    public static final String API_V1_PREFIX = "/api/v1";

    /**
     * Private constructor to prevent instantiation of this utility class
     */
    private ApiConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
