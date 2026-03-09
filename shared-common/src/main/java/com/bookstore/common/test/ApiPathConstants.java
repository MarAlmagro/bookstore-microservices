package com.bookstore.common.test;

public final class ApiPathConstants {

    private ApiPathConstants() {
    }

    // Catalog Service API paths
    public static String CATALOG_BOOKS_API_PATH() {
        return "/api/v1/books";
    }

    public static String CATALOG_BOOK_BY_ID_PATH() {
        return "/api/v1/books/{id}";
    }

    public static String CATALOG_BOOK_STOCK_PATH() {
        return "/api/v1/books/{id}/stock";
    }

    public static String CATALOG_BOOKS_SEARCH_PATH() {
        return "/api/v1/books/search";
    }

    public static String CATALOG_BOOKS_BY_CATEGORY_PATH() {
        return "/api/v1/books/category/{category}";
    }

    public static String CATALOG_BOOKS_BY_AUTHOR_PATH() {
        return "/api/v1/books/author/{author}";
    }

    public static String CATALOG_AVAILABLE_BOOKS_PATH() {
        return "/api/v1/books/available";
    }

    public static String CATALOG_LOW_STOCK_BOOKS_PATH() {
        return "/api/v1/books/low-stock";
    }

    // Order Service API paths
    public static String ORDERS_API_PATH() {
        return "/api/v1/orders";
    }

    public static String ORDER_BY_ID_PATH() {
        return "/api/v1/orders/";
    }

    // JSON path constants (these are not URIs, so they can remain static)
    public static final String JSON_PATH_TITLE = "$.title";
    public static final String JSON_PATH_ID = "$.id";
    public static final String JSON_PATH_ISBN = "$.isbn";

    // Parameter names
    public static final String PARAM_QUANTITY = "quantity";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_THRESHOLD = "threshold";
    public static final String PARAM_QUERY = "query";
    public static final String PARAM_CATEGORY = "category";
    public static final String PARAM_AUTHOR = "author";

    // Test data constants
    public static final String CATEGORY_PROGRAMMING = "Programming";
    public static final String ISBN_9999999999 = "978-9999999999";

    // Job parameter constants
    public static final String PARAM_INPUT_FILE = "inputFile";
    public static final String PARAM_TIMESTAMP = "timestamp";

    // File constants
    public static final String TEMP_FILE_PREFIX = "catalog_import_test";
    public static final String TEMP_FILE_SUFFIX = ".csv";

    // Test credentials
    public static final String TEST_EMAIL = "integration@test.com";
    public static final String TEST_PASSWORD = "password123";
}
