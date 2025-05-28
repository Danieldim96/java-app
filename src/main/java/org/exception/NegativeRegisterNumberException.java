package org.exception;

public class NegativeRegisterNumberException extends RuntimeException {
    private final int registerNumber;

    public NegativeRegisterNumberException(int registerNumber) {
        super(String.format("Register number cannot be negative: %d", registerNumber));
        this.registerNumber = registerNumber;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }
}