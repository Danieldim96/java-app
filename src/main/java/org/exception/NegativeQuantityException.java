package org.exception;

public class NegativeQuantityException extends RuntimeException {
    private final int quantity;

    public NegativeQuantityException(int quantity) {
        super(String.format("Quantity cannot be negative: %d", quantity));
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}