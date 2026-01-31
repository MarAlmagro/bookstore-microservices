package com.bookstore.common.test;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for API paths used in integration tests.
 * Allows customization of URI parameters through application properties.
 */
@Component
@ConfigurationProperties(prefix = "test.api")
public class TestApiProperties {

    private Catalog catalog = new Catalog();
    private Order order = new Order();
    private User user = new User();

    public static class Catalog {
        private String booksPath = "/api/v1/books";
        private String bookByIdPath = "/api/v1/books/{id}";
        private String bookStockPath = "/api/v1/books/{id}/stock";
        private String booksSearchPath = "/api/v1/books/search";
        private String booksByCategoryPath = "/api/v1/books/category/{category}";
        private String booksByAuthorPath = "/api/v1/books/author/{author}";
        private String availableBooksPath = "/api/v1/books/available";
        private String lowStockBooksPath = "/api/v1/books/low-stock";

        // Getters and setters
        public String getBooksPath() { return booksPath; }
        public void setBooksPath(String booksPath) { this.booksPath = booksPath; }
        
        public String getBookByIdPath() { return bookByIdPath; }
        public void setBookByIdPath(String bookByIdPath) { this.bookByIdPath = bookByIdPath; }
        
        public String getBookStockPath() { return bookStockPath; }
        public void setBookStockPath(String bookStockPath) { this.bookStockPath = bookStockPath; }
        
        public String getBooksSearchPath() { return booksSearchPath; }
        public void setBooksSearchPath(String booksSearchPath) { this.booksSearchPath = booksSearchPath; }
        
        public String getBooksByCategoryPath() { return booksByCategoryPath; }
        public void setBooksByCategoryPath(String booksByCategoryPath) { this.booksByCategoryPath = booksByCategoryPath; }
        
        public String getBooksByAuthorPath() { return booksByAuthorPath; }
        public void setBooksByAuthorPath(String booksByAuthorPath) { this.booksByAuthorPath = booksByAuthorPath; }
        
        public String getAvailableBooksPath() { return availableBooksPath; }
        public void setAvailableBooksPath(String availableBooksPath) { this.availableBooksPath = availableBooksPath; }
        
        public String getLowStockBooksPath() { return lowStockBooksPath; }
        public void setLowStockBooksPath(String lowStockBooksPath) { this.lowStockBooksPath = lowStockBooksPath; }
    }

    public static class Order {
        private String ordersPath = "/api/v1/orders";
        private String orderByIdPath = "/api/v1/orders/";

        // Getters and setters
        public String getOrdersPath() { return ordersPath; }
        public void setOrdersPath(String ordersPath) { this.ordersPath = ordersPath; }
        
        public String getOrderByIdPath() { return orderByIdPath; }
        public void setOrderByIdPath(String orderByIdPath) { this.orderByIdPath = orderByIdPath; }
    }

    public static class User {
        private String usersPath = "/api/v1/users";
        private String authPath = "/api/v1/auth";

        // Getters and setters
        public String getUsersPath() { return usersPath; }
        public void setUsersPath(String usersPath) { this.usersPath = usersPath; }
        
        public String getAuthPath() { return authPath; }
        public void setAuthPath(String authPath) { this.authPath = authPath; }
    }

    // Getters and setters
    public Catalog getCatalog() { return catalog; }
    public void setCatalog(Catalog catalog) { this.catalog = catalog; }
    
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
