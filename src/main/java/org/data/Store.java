package org.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a store entity with store-specific attributes.
 */
public class Store implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final String address;
    private final double foodMarkup;
    private final double nonFoodMarkup;
    private final int expirationThreshold;
    private final double expirationDiscount;
    private final Map<Integer, Cashier> registerAssignments;


    public Store(String name, String address, double foodMarkup, double nonFoodMarkup,
                 int expirationThreshold, double expirationDiscount) {
        this.name = name;
        this.address = address;
        this.foodMarkup = foodMarkup;
        this.nonFoodMarkup = nonFoodMarkup;
        this.expirationThreshold = expirationThreshold;
        this.expirationDiscount = expirationDiscount;
        this.registerAssignments = new HashMap<>();
    }


    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public double getFoodMarkup() {
        return foodMarkup;
    }

    public double getNonFoodMarkup() {
        return nonFoodMarkup;
    }

    public int getExpirationThreshold() {
        return expirationThreshold;
    }

    public double getExpirationDiscount() {
        return expirationDiscount;
    }

    public Map<Integer, Cashier> getRegisterAssignments() {
        return new HashMap<>(registerAssignments);
    }

    public void assignCashierToRegister(int registerNumber, Cashier cashier) {
        registerAssignments.put(registerNumber, cashier);
    }

    public Cashier getCashierAtRegister(int registerNumber) {
        return registerAssignments.get(registerNumber);
    }

    public boolean isRegisterAssigned(int registerNumber) {
        return registerAssignments.containsKey(registerNumber);
    }

    @Override
    public String toString() {
        return String.format("Store: %s, Address: %s, Registers: %d",
                name, address, registerAssignments.size());
    }
}