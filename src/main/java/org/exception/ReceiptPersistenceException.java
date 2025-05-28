package org.exception;

public class ReceiptPersistenceException extends RuntimeException {
    
    public ReceiptPersistenceException(String message) {
        super(message);
    }
    
    public ReceiptPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}