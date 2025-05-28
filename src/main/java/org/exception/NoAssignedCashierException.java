package org.exception;

public class NoAssignedCashierException extends RuntimeException {
    private final int registerNumber;

    public NoAssignedCashierException(int registerNumber) {
        super(String.format("No cashier assigned to register %d", registerNumber));
        this.registerNumber = registerNumber;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }
}