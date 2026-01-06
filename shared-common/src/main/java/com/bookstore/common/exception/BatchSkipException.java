package com.bookstore.common.exception;

public class BatchSkipException extends RuntimeException {
    
    public BatchSkipException(String message) {
        super(message);
    }
    
    public BatchSkipException(String message, Throwable cause) {
        super(message, cause);
    }
}
