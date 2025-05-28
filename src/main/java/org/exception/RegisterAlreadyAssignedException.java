package org.exception;

public class RegisterAlreadyAssignedException extends RuntimeException {
    private final int registerNumber;

    public RegisterAlreadyAssignedException(int registerNumber) {
        super(String.format("Register %d is already assigned to a cashier", registerNumber));
        this.registerNumber = registerNumber;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }
}