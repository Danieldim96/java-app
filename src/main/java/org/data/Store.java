package org.data;

import java.io.Serializable;

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

    public Store(String name, String address, double foodMarkup, double nonFoodMarkup,
                 int expirationThreshold, double expirationDiscount) {
        this.name = name;
        this.address = address;
        this.foodMarkup = foodMarkup;
        this.nonFoodMarkup = nonFoodMarkup;
        this.expirationThreshold = expirationThreshold;
        this.expirationDiscount = expirationDiscount;
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
}