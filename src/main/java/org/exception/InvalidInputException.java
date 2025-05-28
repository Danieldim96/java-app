package org.exception;

public class InvalidInputException extends RuntimeException {
    private final String fieldName;
    private final String invalidValue;

    public InvalidInputException(String fieldName, String invalidValue, String message) {
        super(String.format("Invalid input for %s: '%s'. %s", fieldName, invalidValue, message));
        this.fieldName = fieldName;
        this.invalidValue = invalidValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getInvalidValue() {
        return invalidValue;
    }
} 