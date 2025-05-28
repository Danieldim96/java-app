package org.exception;

/**
 * Exception thrown when a negative percentage value is provided.
 */
public class NegativePercentageException extends IllegalArgumentException {
    
    public NegativePercentageException(String message) {
        super(message);
    }
    
    public NegativePercentageException(double percentage) {
        super("Negative percentage value is not allowed: " + percentage);
    }
}