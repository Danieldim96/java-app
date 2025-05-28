package org.exception;

public class CashierNotFoundException extends RuntimeException {
    private final int cashierId;

    public CashierNotFoundException(int cashierId) {
        super(String.format("Cashier not found: %d", cashierId));
        this.cashierId = cashierId;
    }

    public int getCashierId() {
        return cashierId;
    }
}