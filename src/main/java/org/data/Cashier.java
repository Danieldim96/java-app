package org.data;

import java.io.Serializable;
import org.exception.NegativeRegisterNumberException;

public class Cashier implements Serializable {
    private static final long serialVersionUID = 1L;
    private static volatile int nextCashierId = 1;
    private static final Object cashierIdLock = new Object();

    private final int id;
    private final String name;
    private final double monthlySalary;
    private int registerNumber;

    public Cashier(int id, String name, double monthlySalary) {
        this.id = id;
        this.name = name;
        this.monthlySalary = monthlySalary;
        this.registerNumber = -1;
    }

    public Cashier(String name, double monthlySalary) {
        synchronized (cashierIdLock) {
            this.id = nextCashierId++;
        }
        this.name = name;
        this.monthlySalary = monthlySalary;
        this.registerNumber = -1;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getMonthlySalary() {
        return monthlySalary;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(int registerNumber) {
        if (registerNumber < 0) {
            throw new NegativeRegisterNumberException(registerNumber);
        }
        this.registerNumber = registerNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Cashier cashier = (Cashier) o;
        return id == cashier.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s (ID: %d, Salary: %.2f, Register: %d)",
                name, id, monthlySalary, registerNumber);
    }

    public static void resetCashierCounter() {
        synchronized (cashierIdLock) {
            nextCashierId = 1;
        }
    }
}
