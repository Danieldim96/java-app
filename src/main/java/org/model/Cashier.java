package org.model;

import java.io.Serializable;

public class Cashier implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private double monthlySalary;
    private int registerNumber;

    public Cashier(int id, String name, double monthlySalary) {
        this.id = id;
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
        this.registerNumber = registerNumber;
    }

    @Override
    public String toString() {
        return "Cashier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", monthlySalary=" + monthlySalary +
                ", registerNumber=" + registerNumber +
                '}';
    }
} 