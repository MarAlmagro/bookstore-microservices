package com.bookstore.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for the Catalog Service.
 * <p>
 * This microservice manages the book catalog with MySQL persistence.
 * It provides REST APIs for CRUD operations on books.
 * </p>
 *
 * @author Bookstore Team
 * @version 1.0.0
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.bookstore.catalog",
    "com.bookstore.common.exception"  // Include shared exception handlers
})
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogServiceApplication.class, args);
    }
}
